package com.example.booking_service.exceptions;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail notFound(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    @ExceptionHandler({BusinessRuleException.class, IllegalArgumentException.class})
    ProblemDetail conflict(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(FeignException.class)
    ProblemDetail remoteService(FeignException ex) {
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null || status.is5xxServerError())
            status = HttpStatus.BAD_GATEWAY;
        return ProblemDetail.forStatusAndDetail(status, "Customer or Event service request failed");
    }
}
