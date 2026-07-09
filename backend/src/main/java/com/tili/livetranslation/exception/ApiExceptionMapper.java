package com.tili.livetranslation.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException exception) {
        Response.Status status = switch (exception) {
            case NotFoundException e -> Response.Status.NOT_FOUND;
            case InvalidStateTransitionException e -> Response.Status.CONFLICT;
            case SessionFullException e -> Response.Status.CONFLICT;
            case ForbiddenException e -> Response.Status.FORBIDDEN;
            case jakarta.validation.ValidationException e -> Response.Status.BAD_REQUEST;
            default -> Response.Status.INTERNAL_SERVER_ERROR;
        };

        String message = status == Response.Status.INTERNAL_SERVER_ERROR
                ? "Internal server error"
                : exception.getMessage();

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", message, "status", status.getStatusCode()))
                .build();
    }
}
