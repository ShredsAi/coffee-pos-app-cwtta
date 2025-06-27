package ai.shreds.adapter.exceptions;

import ai.shreds.application.exceptions.ApplicationInsufficientStockException;
import ai.shreds.application.exceptions.ApplicationWarehouseInactiveException;
import ai.shreds.domain.exceptions.DomainInvalidMovementTypeException;
import ai.shreds.shared.dtos.SharedErrorResponseDTO;
import ai.shreds.shared.dtos.SharedFieldErrorDTO;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Collections;

@ControllerAdvice
@Order(1)
public class AdapterStockMovementExceptionHandler {

    @ExceptionHandler(ApplicationInsufficientStockException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleInsufficientStockException(
            ApplicationInsufficientStockException ex, WebRequest request) {
        SharedFieldErrorDTO fieldError = SharedFieldErrorDTO.builder()
                .field("quantity")
                .rejectedValue(ex.getRequestedQuantity())
                .message(String.format("Available quantity (%s) is less than requested (%s)", 
                         ex.getAvailableQuantity(), ex.getRequestedQuantity()))
                .build();
                
        SharedErrorResponseDTO error = SharedErrorResponseDTO.builder()
                .type("insufficient-stock")
                .title("Insufficient Stock")
                .status(HttpStatus.CONFLICT.value())
                .detail(String.format("Insufficient stock for product %s in warehouse %s. Requested: %s, Available: %s",
                        ex.getProductId(), ex.getWarehouseId(), ex.getRequestedQuantity(), ex.getAvailableQuantity()))
                .instance(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .violations(Collections.singletonList(fieldError))
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DomainInvalidMovementTypeException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleInvalidMovementTypeException(
            DomainInvalidMovementTypeException ex, WebRequest request) {
        SharedFieldErrorDTO fieldError = SharedFieldErrorDTO.builder()
                .field("movementType")
                .rejectedValue(ex.getMovementType())
                .message("Invalid movement type")
                .build();
                
        SharedErrorResponseDTO error = SharedErrorResponseDTO.builder()
                .type("invalid-movement-type")
                .title("Invalid Movement Type")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .violations(Collections.singletonList(fieldError))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ApplicationWarehouseInactiveException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleWarehouseInactiveException(
            ApplicationWarehouseInactiveException ex, WebRequest request) {
        SharedFieldErrorDTO fieldError = SharedFieldErrorDTO.builder()
                .field("warehouseId")
                .rejectedValue(ex.getWarehouseId())
                .message("Warehouse is inactive")
                .build();
                
        SharedErrorResponseDTO error = SharedErrorResponseDTO.builder()
                .type("warehouse-inactive")
                .title("Warehouse Inactive")
                .status(HttpStatus.CONFLICT.value())
                .detail(String.format("Warehouse %s is inactive and cannot process stock movements", ex.getWarehouseId()))
                .instance(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .violations(Collections.singletonList(fieldError))
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
