"""Verifies the central token lifecycle: caching, expiry-based renewal and 401 recovery."""

import time
from http import HTTPStatus

import pytest
from playwright.sync_api import APIRequestContext

from api_framework.assertions import assert_status
from api_framework.core.api_client import ApiClient
from api_framework.core.token_manager import TokenManager
from api_framework.models import Credentials
from api_framework.services.auth_service import AuthService
from api_framework.services.events_service import EventsService


class SpyAuthService(AuthService):
    """Real AuthService that also counts how many logins happened."""

    def __init__(self, client: ApiClient) -> None:
        super().__init__(client)
        self.login_calls = 0

    def get_token(self, credentials: Credentials) -> str:
        self.login_calls += 1
        return super().get_token(credentials)


class ExpiredTokenProvider:
    """Returns a token the server rejects until refreshed (simulates server-side expiry)."""

    def __init__(self, token_manager: TokenManager) -> None:
        self._token_manager = token_manager
        self._token = "expired-token"
        self.refresh_calls = 0

    def get_token(self) -> str:
        return self._token

    def refresh_token(self) -> str:
        self.refresh_calls += 1
        self._token = self._token_manager.get_token()
        return self._token


@pytest.fixture
def spy_auth_service(public_client: ApiClient) -> SpyAuthService:
    return SpyAuthService(public_client)


@pytest.mark.regression
def test_token_is_generated_once_and_reused(
    spy_auth_service: SpyAuthService, credentials: Credentials
) -> None:
    manager = TokenManager(spy_auth_service, credentials)

    first = manager.get_token()
    second = manager.get_token()

    assert first == second
    assert spy_auth_service.login_calls == 1


@pytest.mark.regression
def test_expired_token_is_regenerated(
    spy_auth_service: SpyAuthService, credentials: Credentials
) -> None:
    now = time.time()
    manager = TokenManager(spy_auth_service, credentials, clock=lambda: now)
    manager.get_token()

    now += 365 * 24 * 60 * 60  # jump one year ahead -> token is expired
    token = manager.get_token()

    assert token
    assert spy_auth_service.login_calls == 2


@pytest.mark.regression
def test_request_is_retried_with_new_token_after_401(
    request_context: APIRequestContext, token_manager: TokenManager
) -> None:
    provider = ExpiredTokenProvider(token_manager)
    events = EventsService(ApiClient(request_context, token_provider=provider))

    response = events.list_events(limit=1)

    assert_status(response, HTTPStatus.OK)
    assert provider.refresh_calls == 1
