"""Events API: one method per endpoint, no assertions."""

from playwright.sync_api import APIResponse

from api_framework.core.api_client import ApiClient
from api_framework.endpoints import Endpoints
from api_framework.models import EventPayload


class EventsService:
    def __init__(self, client: ApiClient) -> None:
        self._client = client

    def create_event(self, event: EventPayload) -> APIResponse:
        return self._client.post(Endpoints.EVENTS, json_body=event.to_dict())

    def get_event(self, event_id: int) -> APIResponse:
        return self._client.get(Endpoints.event(event_id))

    def list_events(self, **params: str | int) -> APIResponse:
        return self._client.get(Endpoints.EVENTS, params=params or None)

    def delete_event(self, event_id: int) -> APIResponse:
        return self._client.delete(Endpoints.event(event_id))
