package com.example.booking_service.exceptions;

import feign.FeignException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.util.concurrent.TimeoutException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
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

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    ProblemDetail seatsChanged(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Seats changed while booking. Please try again.");
    }

    @ExceptionHandler(FeignException.class)
    ProblemDetail remoteService(FeignException ex) {
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null || status.is5xxServerError())
            status = HttpStatus.SERVICE_UNAVAILABLE;
        return ProblemDetail.forStatusAndDetail(status, "Customer or Event service request failed");
    }

    @ExceptionHandler({RetryableException.class, CallNotPermittedException.class})
    ProblemDetail unavailable(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Customer or Event service is unavailable");
    }

    @ExceptionHandler(NoFallbackAvailableException.class)
    ProblemDetail circuitBreakerFailure(NoFallbackAvailableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof FeignException.NotFound)
            return ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        if (cause instanceof FeignException.BadRequest)
            return ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        if (cause instanceof RetryableException || cause instanceof CallNotPermittedException
                || cause instanceof TimeoutException)
            return unavailable(ex);
        if (cause instanceof FeignException feignException)
            return remoteService(feignException);
        return unavailable(ex);
    }
}
