"""Central token management shared by every test.

* The token is generated once and cached for the whole test session.
* Before each request the expiry is checked; an expired (or soon-to-expire)
  token is regenerated automatically.
* `refresh_token()` forces regeneration (used by ApiClient on HTTP 401).
"""

import base64
import json
import logging
import time
from collections.abc import Callable
from dataclasses import dataclass

from api_framework.models import Credentials
from api_framework.services.auth_service import AuthService

logger = logging.getLogger(__name__)


@dataclass(frozen=True, slots=True)
class _CachedToken:
    value: str
    expires_at: float  # epoch seconds


class TokenManager:
    def __init__(
        self,
        auth_service: AuthService,
        credentials: Credentials,
        *,
        default_ttl_seconds: int = 3000,
        refresh_buffer_seconds: int = 60,
        clock: Callable[[], float] = time.time,
    ) -> None:
        self._auth_service = auth_service
        self._credentials = credentials
        self._default_ttl = default_ttl_seconds
        self._refresh_buffer = refresh_buffer_seconds
        self._clock = clock
        self._cached: _CachedToken | None = None

    def get_token(self) -> str:
        """Return a valid token, regenerating it if missing or about to expire."""
        if self._cached is None or self._is_expiring(self._cached):
            self._cached = self._generate()
        return self._cached.value

    def refresh_token(self) -> str:
        """Force a new token regardless of the cached one."""
        self._cached = self._generate()
        return self._cached.value

    def _is_expiring(self, token: _CachedToken) -> bool:
        return self._clock() >= token.expires_at - self._refresh_buffer

    def _generate(self) -> _CachedToken:
        logger.info("Generating a new auth token for %s", self._credentials.email)
        token = self._auth_service.get_token(self._credentials)
        expires_at = _jwt_expiry(token) or self._clock() + self._default_ttl
        return _CachedToken(value=token, expires_at=expires_at)


def _jwt_expiry(token: str) -> float | None:
    """Read the `exp` claim from a JWT (signature is not verified). None if unavailable."""
    try:
        payload = token.split(".")[1]
        padded = payload + "=" * (-len(payload) % 4)
        return float(json.loads(base64.urlsafe_b64decode(padded))["exp"])
    except (IndexError, KeyError, TypeError, ValueError):
        return None
