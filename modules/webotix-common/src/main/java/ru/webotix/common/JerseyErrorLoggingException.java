package ru.webotix.common;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class JerseyErrorLoggingException implements ExceptionMapper<JsonMappingException> {

    private static final Logger log = LoggerFactory.getLogger(JerseyErrorLoggingException.class);

    @Override
    public Response toResponse(JsonMappingException exception) {

        log.error("JSON mapping error at " + exception.getPath(), exception);

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(exception.getMessage())
                .type("text/plain")
                .build();
    }
}
