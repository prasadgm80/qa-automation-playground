"""Auth API: one method per endpoint, no assertions."""

from playwright.sync_api import APIResponse

from api_framework.core.api_client import ApiClient
from api_framework.endpoints import Endpoints
from api_framework.models import Credentials


class AuthenticationError(RuntimeError):
    """Raised when a token cannot be obtained."""


class AuthService:
    def __init__(self, client: ApiClient) -> None:
        self._client = client

    def login(self, credentials: Credentials) -> APIResponse:
        return self._client.post(Endpoints.LOGIN, json_body=credentials.to_dict())

    def get_token(self, credentials: Credentials) -> str:
        response = self.login(credentials)
        if not response.ok:
            raise AuthenticationError(f"Login failed with HTTP {response.status}")
        token = response.json().get("token")
        if not token:
            raise AuthenticationError("Login response did not contain a 'token'")
        return token
