package com.restaurant.order.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import feign.FeignException;

@RestControllerAdvice public class ApiExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class) ProblemDetail missing(ResourceNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,e.getMessage());
    }
    @ExceptionHandler(IllegalArgumentException.class) ProblemDetail bad(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class) ProblemDetail invalid(MethodArgumentNotValidException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,e.getBindingResult().getFieldErrors().stream().map(f->f.getField()+": "+f.getDefaultMessage()).collect(java.util.stream.Collectors.joining("; ")));
    }
    @ExceptionHandler( {
        HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class
    }) ProblemDetail unreadable(Exception e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"Invalid request value or JSON body.");
    }
    @ExceptionHandler(DataIntegrityViolationException.class) ProblemDetail conflict(Exception e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,"This change conflicts with existing records.");
    }
    @ExceptionHandler(FeignException.class) ProblemDetail upstream(FeignException e) {
        return ProblemDetail.forStatusAndDetail(e.status()==404?HttpStatus.BAD_REQUEST:HttpStatus.SERVICE_UNAVAILABLE,e.status()==404?"A selected dish no longer exists.":"Menu service is unavailable. Try again shortly.");
    }
}
