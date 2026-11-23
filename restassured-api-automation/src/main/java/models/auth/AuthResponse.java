package models.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response of the auth endpoints, carrying the JWT token and the user summary. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthResponse(boolean success, String token, User user) {

    /** Minimal authenticated user details returned alongside the token. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record User(long id, String email) {
    }
}
