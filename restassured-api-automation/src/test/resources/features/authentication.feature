Feature: Authentication

  Obtaining a JWT bearer token is the entry point for all protected endpoints.

  Scenario: Log in with valid credentials
    When I log in with the configured credentials
    Then the login succeeds and a token is returned
