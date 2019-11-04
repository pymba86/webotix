package ru.webotix.common;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class JerseyMappingErrorLoggingExceptionHandler implements ExceptionMapper<JsonMappingException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JerseyMappingErrorLoggingExceptionHandler.class);

    @Override
    public Response toResponse(JsonMappingException exception) {
        LOGGER.error("JSON mapping error at " + exception.getPath(), exception);
        return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).type("text/plain").build();
    }
}
