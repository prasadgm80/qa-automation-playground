package models.auth;

/** Request body for {@code POST /auth/login} and {@code POST /auth/register}. */
public record LoginRequest(String email, String password) {
}
