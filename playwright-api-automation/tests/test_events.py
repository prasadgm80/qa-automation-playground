from http import HTTPStatus

import pytest
from playwright.sync_api import APIRequestContext

from api_framework.assertions import assert_fields_match, assert_status, assert_success
from api_framework.core.api_client import ApiClient
from api_framework.data_factory import build_event
from api_framework.services.events_service import EventsService


@pytest.mark.smoke
@pytest.mark.parametrize("city", ["Bangalore", "Mumbai"])
def test_create_event(events_service: EventsService, cleanup_events: list[int], city: str) -> None:
    payload = build_event(city=city)

    response = events_service.create_event(payload)

    assert_status(response, HTTPStatus.CREATED)
    body = response.json()
    cleanup_events.append(body["data"]["id"])

    assert_success(body, expected_message="Event created successfully")
    assert body["data"]["city"] == city
    assert_fields_match(
        body["data"],
        {
            "title": payload.title,
            "description": payload.description,
            "category": payload.category,
            "venue": payload.venue,
            "totalSeats": payload.total_seats,
            "availableSeats": payload.total_seats,
            "imageUrl": payload.image_url,
        },
    )
    # The API returns price as a string (e.g. "1500")
    assert float(body["data"]["price"]) == payload.price


@pytest.mark.regression
def test_get_event_by_id(events_service: EventsService, created_event: dict) -> None:
    response = events_service.get_event(created_event["id"])

    assert_status(response, HTTPStatus.OK)
    body = response.json()
    assert_success(body)
    assert_fields_match(
        body["data"],
        {key: created_event[key] for key in ("id", "title", "city", "venue", "eventDate")},
    )


@pytest.mark.regression
def test_delete_event(events_service: EventsService, created_event: dict) -> None:
    delete_response = events_service.delete_event(created_event["id"])

    assert_status(delete_response, HTTPStatus.OK)
    assert_success(delete_response.json(), expected_message="Event deleted successfully")

    get_response = events_service.get_event(created_event["id"])
    assert_status(get_response, HTTPStatus.NOT_FOUND)


@pytest.mark.regression
def test_create_event_without_token_is_unauthorized(request_context: APIRequestContext) -> None:
    unauthenticated_events = EventsService(ApiClient(request_context))

    response = unauthenticated_events.create_event(build_event())

    assert_status(response, HTTPStatus.UNAUTHORIZED)
    assert response.json()["success"] is False
