package ai.shreds.shared.value_objects;

import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import ai.shreds.shared.enums.SharedStockMovementTypeEnum;

/**
 * Value object representing query parameters for stock ledger filtering.
 * Used to filter and paginate stock movement history in the ledger.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedStockLedgerQueryParams {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SharedStockMovementTypeEnum movementType;
    
    @Min(value = 0, message = "Page must be non-negative")
    @Builder.Default
    private int page = 0;
    
    @Min(value = 1, message = "Size must be at least 1")
    @Builder.Default
    private int size = 20;
    
    private String sortBy;
    
    @Builder.Default
    private boolean sortAscending = false;
    
    /**
     * Gets the start date as an Optional.
     *
     * @return Optional containing the start date or empty if not specified
     */
    public Optional<LocalDateTime> getStartDateOpt() {
        return Optional.ofNullable(startDate);
    }
    
    /**
     * Gets the end date as an Optional.
     *
     * @return Optional containing the end date or empty if not specified
     */
    public Optional<LocalDateTime> getEndDateOpt() {
        return Optional.ofNullable(endDate);
    }
    
    /**
     * Gets the movement type as an Optional.
     *
     * @return Optional containing the movement type or empty if not specified
     */
    public Optional<SharedStockMovementTypeEnum> getMovementTypeOpt() {
        return Optional.ofNullable(movementType);
    }
    
    /**
     * Gets the sort field with a default if not specified.
     *
     * @return The field name to sort by, defaults to "performedAt" if not specified
     */
    public String getSortByOrDefault() {
        return sortBy != null ? sortBy : "performedAt";
    }
    
    /**
     * Creates a new instance with default pagination values.
     *
     * @return A new query params object with default pagination
     */
    public static SharedStockLedgerQueryParams createDefault() {
        return SharedStockLedgerQueryParams.builder().build();
    }
    
    /**
     * Creates a date range query for a specific date (start and end of day).
     *
     * @param date The date to query for (time components are ignored)
     * @return Query params for the specified date
     */
    public static SharedStockLedgerQueryParams forDate(java.time.LocalDate date) {
        return SharedStockLedgerQueryParams.builder()
            .startDate(date.atStartOfDay())
            .endDate(date.plusDays(1).atStartOfDay())
            .build();
    }
    
    /**
     * Creates a query for a specific movement type.
     *
     * @param type The movement type to filter by
     * @return Query params for the specified movement type
     */
    public static SharedStockLedgerQueryParams forMovementType(SharedStockMovementTypeEnum type) {
        return SharedStockLedgerQueryParams.builder()
            .movementType(type)
            .build();
    }
}