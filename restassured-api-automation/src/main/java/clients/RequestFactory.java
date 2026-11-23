package clients;

import static io.restassured.RestAssured.given;

import config.ConfigReader;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import utils.RestAssuredLogFilter;

/** Central place that builds pre-configured REST Assured requests for every service. */
public final class RequestFactory {

    private static final ConfigReader CONFIG = ConfigReader.load();
    private static final RequestSpecification BASE_SPEC = buildBaseSpec();

    private RequestFactory() {
    }

    /** Returns a fresh request with base URI, JSON content type, timeout and logging applied. */
    public static RequestSpecification request() {
        return given().spec(BASE_SPEC);
    }

    /** Returns a request pre-loaded with a bearer token for authenticated endpoints. */
    public static RequestSpecification authorizedRequest(String bearerToken) {
        return request().header("Authorization", "Bearer " + bearerToken);
    }

    private static RequestSpecification buildBaseSpec() {
        int timeoutMs = CONFIG.getInt("http.timeout.seconds", 30) * 1000;
        RestAssuredConfig config = RestAssuredConfig.config().httpClient(
                HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", timeoutMs)
                        .setParam("http.socket.timeout", timeoutMs));

        return new RequestSpecBuilder()
                .setBaseUri(CONFIG.getRequired("base.uri"))
                .setContentType(ContentType.JSON)
                .setConfig(config)
                .addFilter(new RestAssuredLogFilter())
                .build();
    }
}
