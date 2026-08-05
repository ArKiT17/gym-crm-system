package com.maxlikarenko.gymcrmsystem.controller;

import com.maxlikarenko.gymcrmsystem.exception.ConflictException;
import com.maxlikarenko.gymcrmsystem.exception.ResourceNotFoundException;
import com.maxlikarenko.gymcrmsystem.exception.UnauthorizedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleValidationReturnsFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(
                java.util.List.of(
                        new FieldError("request", "firstName", "First name is required"),
                        new FieldError("request", "lastName", null)
                )
        );
        BindException exception = mock(BindException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ProblemDetail problem = handler.handleValidation(exception);

        assertProblem(problem, HttpStatus.BAD_REQUEST, "Request validation failed");
        assertEquals(
                java.util.Map.of(
                        "firstName", "First name is required",
                        "lastName", "Invalid value"
                ),
                problem.getProperties().get("errors")
        );
    }

    @Test
    void handleConstraintViolationReturnsConstraintErrors() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        jakarta.validation.Path path = mock(jakarta.validation.Path.class);
        when(path.toString()).thenReturn("request.firstName");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("First name is required");

        ProblemDetail problem = handler.handleConstraintViolation(
                new ConstraintViolationException(Set.of(violation))
        );

        assertProblem(problem, HttpStatus.BAD_REQUEST, "Request validation failed");
        assertEquals(
                java.util.Map.of("request.firstName", "First name is required"),
                problem.getProperties().get("errors")
        );
    }

    @Test
    void handleUnreadableMessageReturnsBadRequest() {
        ProblemDetail problem = handler.handleUnreadableMessage(
                mock(org.springframework.http.converter.HttpMessageNotReadableException.class)
        );

        assertProblem(problem, HttpStatus.BAD_REQUEST, "Request body is missing or malformed");
    }

    @Test
    void handleInvalidRequestParameterReturnsBadRequest() {
        ProblemDetail problem = handler.handleInvalidRequestParameter(
                mock(MethodArgumentTypeMismatchException.class)
        );

        assertProblem(problem, HttpStatus.BAD_REQUEST, "Request parameter is missing or invalid");
    }

    @Test
    void handleMethodNotSupportedIncludesAllowedMethods() {
        HttpRequestMethodNotSupportedException exception =
                mock(HttpRequestMethodNotSupportedException.class);
        when(exception.getSupportedHttpMethods()).thenReturn(Set.of(HttpMethod.GET, HttpMethod.POST));

        ProblemDetail problem = handler.handleMethodNotSupported(exception);

        assertProblem(problem, HttpStatus.METHOD_NOT_ALLOWED, "HTTP method is not supported");
        assertEquals(Set.of(HttpMethod.GET, HttpMethod.POST), problem.getProperties().get("allowedMethods"));
    }

    @Test
    void handleMediaTypeNotSupportedReturnsUnsupportedMediaType() {
        ProblemDetail problem = handler.handleMediaTypeNotSupported(
                mock(HttpMediaTypeNotSupportedException.class)
        );

        assertProblem(problem, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content type is not supported");
    }

    @Test
    void handleNoHandlerFoundReturnsNotFound() {
        ProblemDetail problem = handler.handleNoHandlerFound(
                mock(org.springframework.web.servlet.NoHandlerFoundException.class)
        );

        assertProblem(problem, HttpStatus.NOT_FOUND, "Resource not found");
    }

    @Test
    void handleResourceNotFoundPreservesExceptionMessage() {
        ProblemDetail problem = handler.handleResourceNotFound(
                new ResourceNotFoundException("Trainee was not found")
        );

        assertProblem(problem, HttpStatus.NOT_FOUND, "Trainee was not found");
    }

    @Test
    void handleResourceNotFoundUsesDefaultMessageWhenMissing() {
        ProblemDetail problem = handler.handleResourceNotFound(new ResourceNotFoundException(""));

        assertProblem(problem, HttpStatus.NOT_FOUND, "Requested entity was not found");
    }

    @Test
    void handleUnauthorizedReturnsUnauthorized() {
        ProblemDetail problem = handler.handleUnauthorized(
                new UnauthorizedException("Invalid username or password")
        );

        assertProblem(problem, HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    @Test
    void handleConflictReturnsConflict() {
        ProblemDetail problem = handler.handleConflict(
                new ConflictException("Trainer has no specialization")
        );

        assertProblem(problem, HttpStatus.CONFLICT, "Trainer has no specialization");
    }

    @Test
    void handleIllegalArgumentReturnsBadRequest() {
        ProblemDetail problem = handler.handleIllegalArgument(
                new IllegalArgumentException("Invalid username")
        );

        assertProblem(problem, HttpStatus.BAD_REQUEST, "Invalid username");
    }

    @Test
    void handleDataIntegrityViolationHidesDatabaseDetails() {
        ProblemDetail problem = handler.handleDataIntegrityViolation(
                mock(DataIntegrityViolationException.class)
        );

        assertProblem(problem, HttpStatus.CONFLICT, "Request conflicts with existing data");
    }

    @Test
    void handleUnexpectedExceptionReturnsInternalServerError() {
        ProblemDetail problem = handler.handleUnexpectedException(
                new RuntimeException("internal details")
        );

        assertProblem(problem, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private void assertProblem(ProblemDetail problem, HttpStatus status, String detail) {
        assertEquals(status.value(), problem.getStatus());
        assertEquals(detail, problem.getDetail());
    }
}
