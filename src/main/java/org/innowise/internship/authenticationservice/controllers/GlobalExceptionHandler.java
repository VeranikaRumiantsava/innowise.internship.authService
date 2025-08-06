package org.innowise.internship.authenticationservice.controllers;

import java.util.List;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.innowise.internship.authenticationservice.dto.errors.ErrorResponse;
import org.innowise.internship.authenticationservice.exceptions.UnauthorizedException;
import org.innowise.internship.authenticationservice.exceptions.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(RuntimeException ex) {
        log.warn("User already exists: {}", ex.getMessage(), ex);

        return buildErrorResponse(List.of(
                        ex.getMessage()),
                HttpStatus.BAD_REQUEST,
                "User already exists");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwtException(RuntimeException ex) {
        return buildErrorResponse(List.of(
                        ex.getMessage()),
                HttpStatus.UNAUTHORIZED,
                "Expired token");
    }

    @ExceptionHandler({JwtException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleJwtInvalidExceptions(RuntimeException ex) {
        return buildErrorResponse(List.of(
                        ex.getMessage()),
                HttpStatus.UNAUTHORIZED,
                "Invalid token");
    }


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(RuntimeException ex) {
        log.warn("Incorrect login or password: {}", ex.getMessage(), ex);

        return buildErrorResponse(List.of(
                        ex.getMessage()),
                HttpStatus.UNAUTHORIZED,
                "Incorrect login or password");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(RuntimeException ex) {
        log.warn("User doesn't exists: {}", ex.getMessage(), ex);

        return buildErrorResponse(List.of(
                        ex.getMessage()),
                HttpStatus.UNAUTHORIZED,
                "User doesn't exists");
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(RuntimeException ex) {
        log.warn("Unauthorized: {}", ex.getMessage(), ex);

        return buildErrorResponse(List.of(
                        ex.getMessage()),
                HttpStatus.UNAUTHORIZED,
                "Unauthorized");
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();

        log.info("Validation failed: {} errors", errors.size());
        errors.forEach(error -> log.debug("Validation error: {}", error));

        return buildErrorResponse (
                errors,
                HttpStatus.BAD_REQUEST,
                "Bad request"
        );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedExceptions(Exception ex) {
        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);

        return buildErrorResponse(
                List.of("Unexpected error occurred"),
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error"
        );
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(List<String> messages, HttpStatus status, String error) {
        ErrorResponse errorResponse = new ErrorResponse(messages, status.value(), error);
        return new ResponseEntity<>(errorResponse, status);
    }
}
