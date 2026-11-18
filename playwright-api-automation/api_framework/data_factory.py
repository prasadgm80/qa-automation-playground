"""Test data builders. Tests override only the fields they care about."""

import uuid
from dataclasses import replace
from datetime import UTC, datetime, timedelta

from api_framework.models import EventPayload


def future_iso_date(days_ahead: int = 30) -> str:
    """ISO-8601 UTC timestamp in the format the API expects (e.g. 2026-12-18T09:00:00.000Z)."""
    date = datetime.now(UTC) + timedelta(days=days_ahead)
    return date.strftime("%Y-%m-%dT%H:%M:%S.000Z")


def build_event(**overrides: object) -> EventPayload:
    """Return a valid event payload with a unique title. Override any field via kwargs."""
    event = EventPayload(
        title=f"Tech Summit 2026 - {uuid.uuid4().hex[:8]}",
        description="A premier technology conference.",
        category="Conference",
        venue="Bangalore International Centre",
        city="Bangalore",
        event_date=future_iso_date(),
        price=1500,
        total_seats=500,
        image_url="https://example.com/banner.jpg",
    )
    return replace(event, **overrides)
