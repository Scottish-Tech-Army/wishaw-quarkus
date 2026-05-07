package org.scottishtecharmy.wishaw.quarkus.security;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

/**
 * Global exception mapper — handles all exceptions and returns structured JSON.
 * Known exceptions (auth, validation) return appropriate 4xx codes.
 * Unknown exceptions return 500 and are logged as errors with full stack trace.
 */
@Provider
@Priority(Priorities.USER)
public class UnauthorisedExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(UnauthorisedExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {

        // 401 — not authenticated
        if (exception instanceof AuthenticationFailedException
                || exception instanceof UnauthorizedException) {
            LOG.debugf("Authentication failed: %s", exception.getMessage());
            return jsonResponse(Response.Status.UNAUTHORIZED, "Not authenticated");
        }

        // 403 — authenticated but not authorised
        if (exception instanceof ForbiddenException) {
            LOG.debugf("Access forbidden: %s", exception.getMessage());
            return jsonResponse(Response.Status.FORBIDDEN, "Forbidden");
        }

        // 4xx — explicit WebApplicationException (e.g. 400, 404 thrown by resources)
        if (exception instanceof WebApplicationException wae) {
            int status = wae.getResponse().getStatus();
            String message = exception.getMessage() != null ? exception.getMessage() : "Request error";
            LOG.debugf("WebApplicationException [%d]: %s", status, message);
            return jsonResponse(status, message);
        }

        // 500 — unexpected error, log full stack trace
        LOG.errorf(exception, "Unhandled exception: %s", exception.getMessage());
        return jsonResponse(Response.Status.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private static Response jsonResponse(Response.Status status, String message) {
        return jsonResponse(status.getStatusCode(), message);
    }

    private static Response jsonResponse(int status, String message) {
        String body = "{\"error\":\"" + message.replace("\"", "\\\"") + "\"}";
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
