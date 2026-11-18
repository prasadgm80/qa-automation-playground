"""Shared fixtures: this is where the small building blocks are wired together.

settings -> request_context -> ApiClient -> AuthService -> TokenManager
                                         -> ApiClient(with token) -> EventsService
"""

from collections.abc import Iterator
from http import HTTPStatus

import pytest
from playwright.sync_api import APIRequestContext, Playwright, sync_playwright

from api_framework.assertions import assert_status
from api_framework.config import Settings, get_settings
from api_framework.core.api_client import ApiClient
from api_framework.core.token_manager import TokenManager
from api_framework.data_factory import build_event
from api_framework.models import Credentials
from api_framework.services.auth_service import AuthService
from api_framework.services.events_service import EventsService


@pytest.fixture(scope="session")
def settings() -> Settings:
    return get_settings()


@pytest.fixture(scope="session")
def credentials(settings: Settings) -> Credentials:
    return Credentials(email=settings.user_email, password=settings.user_password)


@pytest.fixture(scope="session")
def playwright() -> Iterator[Playwright]:
    with sync_playwright() as pw:
        yield pw


@pytest.fixture(scope="session")
def request_context(playwright: Playwright, settings: Settings) -> Iterator[APIRequestContext]:
    context = playwright.request.new_context(
        base_url=settings.base_url,
        extra_http_headers={"Accept": "application/json"},
        timeout=settings.request_timeout_ms,
    )
    yield context
    context.dispose()


@pytest.fixture(scope="session")
def public_client(request_context: APIRequestContext) -> ApiClient:
    """Client without authentication (login, negative tests)."""
    return ApiClient(request_context)


@pytest.fixture(scope="session")
def auth_service(public_client: ApiClient) -> AuthService:
    return AuthService(public_client)


@pytest.fixture(scope="session")
def token_manager(
    auth_service: AuthService, credentials: Credentials, settings: Settings
) -> TokenManager:
    """The single token source shared by every test in the session."""
    return TokenManager(
        auth_service,
        credentials,
        default_ttl_seconds=settings.token_ttl_seconds,
        refresh_buffer_seconds=settings.token_refresh_buffer_seconds,
    )


@pytest.fixture(scope="session")
def api_client(request_context: APIRequestContext, token_manager: TokenManager) -> ApiClient:
    """Authenticated client: always sends a valid, auto-refreshed bearer token."""
    return ApiClient(request_context, token_provider=token_manager)


@pytest.fixture(scope="session")
def events_service(api_client: ApiClient) -> EventsService:
    return EventsService(api_client)


@pytest.fixture
def cleanup_events(events_service: EventsService) -> Iterator[list[int]]:
    """Append created event ids to this list; they are deleted after the test."""
    event_ids: list[int] = []
    yield event_ids
    for event_id in event_ids:
        events_service.delete_event(event_id)


@pytest.fixture
def created_event(events_service: EventsService, cleanup_events: list[int]) -> dict:
    """An event that exists on the server for the duration of the test."""
    response = events_service.create_event(build_event())
    assert_status(response, HTTPStatus.CREATED)
    event = response.json()["data"]
    cleanup_events.append(event["id"])
    return event
