package clients;

import io.restassured.response.Response;
import models.auth.LoginRequest;

/** Thin wrapper over the EventHub auth endpoints. */
public final class AuthService {

    private static final String LOGIN_PATH = "/auth/login";

    private AuthService() {
    }

    /** Logs in with the given credentials and returns the raw response. */
    public static Response login(String email, String password) {
        return RequestFactory.request()
                .body(new LoginRequest(email, password))
                .post(LOGIN_PATH);
    }
}
