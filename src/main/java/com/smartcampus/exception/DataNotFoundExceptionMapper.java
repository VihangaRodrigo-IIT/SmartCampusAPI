package com.smartcampus.exception;

import com.smartcampus.model.ErrorMessage;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DataNotFoundExceptionMapper 
    implements ExceptionMapper<DataNotFoundException> {

    @Override
    public Response toResponse(DataNotFoundException exception) {
        ErrorMessage error = new ErrorMessage(
            exception.getMessage(),
            404,
            "http://smartcampus.ac.uk/api/docs/errors"
        );
        return Response.status(Response.Status.NOT_FOUND)
                       .entity(error)
                       .build();
    }
}