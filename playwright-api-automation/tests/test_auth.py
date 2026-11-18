from http import HTTPStatus

import pytest
from playwright.sync_api import expect

from api_framework.assertions import assert_error, assert_status
from api_framework.models import Credentials
from api_framework.services.auth_service import AuthService


@pytest.mark.smoke
def test_login_with_valid_credentials_returns_token(
    auth_service: AuthService, credentials: Credentials
) -> None:
    response = auth_service.login(credentials)

    expect(response).to_be_ok()
    body = response.json()
    assert body["success"] is True
    assert isinstance(body["token"], str)
    assert body["token"], "Token should not be empty"


@pytest.mark.regression
def test_login_with_wrong_password_is_rejected(
    auth_service: AuthService, credentials: Credentials
) -> None:
    invalid = Credentials(email=credentials.email, password="WrongPassword123")

    response = auth_service.login(invalid)

    assert_status(response, HTTPStatus.BAD_REQUEST)
    assert_error(response.json(), "Invalid email or password")
