"""Reusable assertions with readable failure messages."""

from typing import Any

from playwright.sync_api import APIResponse


def assert_status(response: APIResponse, expected: int) -> None:
    assert response.status == expected, (
        f"Expected HTTP {expected} but got {response.status} for {response.url}\n"
        f"Body: {response.text()}"
    )


def assert_success(body: dict[str, Any], expected_message: str | None = None) -> None:
    assert body.get("success") is True, f"Expected success=true, got: {body}"
    if expected_message is not None:
        assert body.get("message") == expected_message, (
            f"Expected message '{expected_message}', got '{body.get('message')}'"
        )


def assert_error(body: dict[str, Any], expected_error: str) -> None:
    assert body.get("success") is False, f"Expected success=false, got: {body}"
    assert body.get("error") == expected_error, (
        f"Expected error '{expected_error}', got '{body.get('error')}'"
    )


def assert_fields_match(actual: dict[str, Any], expected: dict[str, Any]) -> None:
    """Every key in `expected` must exist in `actual` with an equal value."""
    mismatches = {
        key: {"expected": value, "actual": actual.get(key, "<missing>")}
        for key, value in expected.items()
        if actual.get(key) != value
    }
    assert not mismatches, f"Field mismatches: {mismatches}"
