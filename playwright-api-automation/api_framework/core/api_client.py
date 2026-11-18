"""Thin wrapper around Playwright's `APIRequestContext`.

Responsibilities:
* attach the bearer token on every call (fetched from the central TokenManager)
* if the server answers 401, regenerate the token and retry the call once
* log every request/response with secrets redacted
"""

import json
import logging
from http import HTTPStatus
from typing import Any, Protocol

from playwright.sync_api import APIRequestContext, APIResponse

logger = logging.getLogger(__name__)

_SENSITIVE_KEYS = frozenset({"password", "token", "accessToken", "refreshToken"})


class TokenProvider(Protocol):
    def get_token(self) -> str: ...

    def refresh_token(self) -> str: ...


class ApiClient:
    def __init__(
        self,
        request_context: APIRequestContext,
        token_provider: TokenProvider | None = None,
    ) -> None:
        self._request = request_context
        self._token_provider = token_provider

    def get(self, endpoint: str, *, params: dict[str, Any] | None = None) -> APIResponse:
        return self._send("GET", endpoint, params=params)

    def post(self, endpoint: str, *, json_body: Any = None) -> APIResponse:
        return self._send("POST", endpoint, data=json_body)

    def put(self, endpoint: str, *, json_body: Any = None) -> APIResponse:
        return self._send("PUT", endpoint, data=json_body)

    def patch(self, endpoint: str, *, json_body: Any = None) -> APIResponse:
        return self._send("PATCH", endpoint, data=json_body)

    def delete(self, endpoint: str) -> APIResponse:
        return self._send("DELETE", endpoint)

    def _send(self, method: str, endpoint: str, **kwargs: Any) -> APIResponse:
        response = self._fetch(method, endpoint, **kwargs)
        if response.status == HTTPStatus.UNAUTHORIZED and self._token_provider is not None:
            logger.warning("401 on %s %s - regenerating token and retrying once", method, endpoint)
            self._token_provider.refresh_token()
            response = self._fetch(method, endpoint, **kwargs)
        return response

    def _fetch(self, method: str, endpoint: str, **kwargs: Any) -> APIResponse:
        headers = {}
        if self._token_provider is not None:
            headers["Authorization"] = f"Bearer {self._token_provider.get_token()}"

        logger.info("--> %s %s %s", method, endpoint, _format_body(kwargs.get("data")))
        response = self._request.fetch(endpoint, method=method, headers=headers, **kwargs)
        logger.info("<-- %s %s %s", response.status, endpoint, _response_body(response))
        return response


def _redact(payload: Any) -> Any:
    if isinstance(payload, dict):
        return {k: "***" if k in _SENSITIVE_KEYS else _redact(v) for k, v in payload.items()}
    if isinstance(payload, list):
        return [_redact(item) for item in payload]
    return payload


def _format_body(body: Any) -> str:
    return "" if body is None else json.dumps(_redact(body))


def _response_body(response: APIResponse) -> str:
    try:
        return _format_body(response.json())
    except ValueError:
        return response.text()
