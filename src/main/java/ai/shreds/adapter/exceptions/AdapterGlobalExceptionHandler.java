package ai.shreds.adapter.exceptions;

import ai.shreds.application.exceptions.ApplicationOptimisticLockException;
import ai.shreds.domain.exceptions.DomainEntityNotFoundException;
import ai.shreds.domain.exceptions.DomainValidationException;
import ai.shreds.shared.dtos.SharedErrorResponseDTO;
import ai.shreds.shared.dtos.SharedFieldErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class AdapterGlobalExceptionHandler {

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleValidationException(
            DomainValidationException ex, WebRequest request) {
        SharedFieldErrorDTO fieldError = SharedFieldErrorDTO.builder()
                .field(ex.getFieldName())
                .rejectedValue(ex.getRejectedValue())
                .message(ex.getMessage())
                .build();
                
        SharedErrorResponseDTO error = SharedErrorResponseDTO.builder()
                .type("validation-error")
                .title("Validation Error")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .violations(List.of(fieldError))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DomainEntityNotFoundException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleNotFoundException(
            DomainEntityNotFoundException ex, WebRequest request) {
        SharedErrorResponseDTO error = SharedErrorResponseDTO.builder()
                .type("not-found")
                .title("Resource Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .instance(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ApplicationOptimisticLockException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleOptimisticLockException(
            ApplicationOptimisticLockException ex, WebRequest request) {
        SharedErrorResponseDTO error = SharedErrorResponseDTO.builder()
                .type("conflict")
                .title("Concurrent Modification")
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        List<SharedFieldErrorDTO> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> SharedFieldErrorDTO.builder()
                        .field(error.getField())
                        .rejectedValue(error.getRejectedValue())
                        .message(error.getDefaultMessage())
                        .objectName(error.getObjectName())
                        .code(error.getCode())
                        .build())
                .collect(Collectors.toList());

        SharedErrorResponseDTO error = SharedErrorResponseDTO.builder()
                .type("validation-error")
                .title("Validation Error")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Invalid request parameters")
                .instance(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .violations(fieldErrors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SharedErrorResponseDTO> handleGenericException(
            Exception ex, WebRequest request) {
        SharedErrorResponseDTO error = SharedErrorResponseDTO.builder()
                .type("internal-error")
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An unexpected error occurred")
                .instance(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
