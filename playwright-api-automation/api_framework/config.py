"""Central configuration.

Works out of the box with the defaults below. Any value can be overridden with an
environment variable or a local `.env` file (see .env.example).
"""

import os
from dataclasses import dataclass
from functools import lru_cache

from dotenv import load_dotenv

DEFAULT_BASE_URL = "https://api.eventhub.rahulshettyacademy.com"
# Dummy test-account credentials (not sensitive)
DEFAULT_USER_EMAIL = "prasadgm80@gmail.com"
DEFAULT_USER_PASSWORD = "prasadgm80"


@dataclass(frozen=True, slots=True)
class Settings:
    base_url: str
    user_email: str
    user_password: str
    request_timeout_ms: float
    token_ttl_seconds: int
    token_refresh_buffer_seconds: int


def _env(name: str, default: str) -> str:
    """Environment value, falling back to the default when unset or empty."""
    return os.getenv(name, "").strip() or default


@lru_cache(maxsize=1)
def get_settings() -> Settings:
    """Build the settings once per test run."""
    load_dotenv()
    return Settings(
        base_url=_env("BASE_URL", DEFAULT_BASE_URL).rstrip("/"),
        user_email=_env("API_USER_EMAIL", DEFAULT_USER_EMAIL),
        user_password=_env("API_USER_PASSWORD", DEFAULT_USER_PASSWORD),
        request_timeout_ms=float(_env("REQUEST_TIMEOUT_MS", "30000")),
        token_ttl_seconds=int(_env("TOKEN_TTL_SECONDS", "3000")),
        token_refresh_buffer_seconds=int(_env("TOKEN_REFRESH_BUFFER_SECONDS", "60")),
    )
