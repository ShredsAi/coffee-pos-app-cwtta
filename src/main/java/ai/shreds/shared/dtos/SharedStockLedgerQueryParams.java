package ai.shreds.shared.dtos;

import java.time.LocalDateTime;
import ai.shreds.shared.enums.SharedStockMovementTypeEnum;

/**
 * Query parameters for fetching stock ledger entries.
 */
public class SharedStockLedgerQueryParams {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SharedStockMovementTypeEnum movementType;
    private Integer page = 0;
    private Integer size = 10;

    public SharedStockLedgerQueryParams() {
    }

    public SharedStockLedgerQueryParams(LocalDateTime startDate, LocalDateTime endDate,
                                        SharedStockMovementTypeEnum movementType,
                                        Integer page, Integer size) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.movementType = movementType;
        this.page = page;
        this.size = size;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public SharedStockMovementTypeEnum getMovementType() {
        return movementType;
    }

    public void setMovementType(SharedStockMovementTypeEnum movementType) {
        this.movementType = movementType;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}