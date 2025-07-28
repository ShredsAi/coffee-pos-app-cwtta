package ai.shreds.adapter.exceptions;

import ai.shreds.shared.dtos.SharedErrorResponse;
import ai.shreds.shared.exceptions.SharedBadRequestException;
import ai.shreds.shared.exceptions.SharedConflictException;
import ai.shreds.shared.exceptions.SharedNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class AdapterExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<SharedErrorResponse> handleValidationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));
        SharedErrorResponse response = SharedErrorResponse.validationError(errors, request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SharedNotFoundException.class)
    public ResponseEntity<SharedErrorResponse> handleNotFound(
            SharedNotFoundException ex,
            HttpServletRequest request) {
        SharedErrorResponse response = SharedErrorResponse.notFound(ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SharedConflictException.class)
    public ResponseEntity<SharedErrorResponse> handleConflict(
            SharedConflictException ex,
            HttpServletRequest request) {
        SharedErrorResponse response = SharedErrorResponse.conflict(ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(SharedBadRequestException.class)
    public ResponseEntity<SharedErrorResponse> handleBadRequest(
            SharedBadRequestException ex,
            HttpServletRequest request) {
        SharedErrorResponse response = SharedErrorResponse.badRequest(ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SharedErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        SharedErrorResponse response = SharedErrorResponse.internalError(ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
