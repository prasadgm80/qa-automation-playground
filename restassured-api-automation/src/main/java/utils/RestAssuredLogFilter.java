package utils;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Logs every request and response through SLF4J so failures are always traceable. */
public class RestAssuredLogFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(RestAssuredLogFilter.class);

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {
        LOG.info("--> {} {}", requestSpec.getMethod(), requestSpec.getURI());
        if (requestSpec.getBody() != null) {
            LOG.info("    request body: {}", requestSpec.getBody().toString());
        }

        Response response = ctx.next(requestSpec, responseSpec);

        LOG.info("<-- {} ({} ms)", response.getStatusCode(), response.getTime());
        String body = response.getBody().asString();
        if (body != null && !body.isBlank()) {
            LOG.info("    response body: {}", body);
        }
        return response;
    }
}
