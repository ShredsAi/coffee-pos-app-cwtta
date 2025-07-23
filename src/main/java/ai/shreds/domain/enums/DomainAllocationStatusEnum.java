package ai.shreds.domain.enums;

/**
 * Enumeration representing the different states of a stock allocation.
 * Allocations track inventory items that have been allocated to specific batches
 * for outbound fulfillment.
 */
public enum DomainAllocationStatusEnum {
    /**
     * Allocation is pending, awaiting confirmation
     */
    PENDING,
    
    /**
     * Allocation has been confirmed and stock is allocated to specific batches
     */
    CONFIRMED,
    
    /**
     * Allocated stock has been shipped or consumed
     */
    SHIPPED,
    
    /**
     * Allocation has been cancelled and stock is released
     */
    CANCELLED;
    
    /**
     * Checks if this status indicates the allocation is actively holding stock.
     * 
     * @return true if the allocation is still holding stock
     */
    public boolean isHoldingStock() {
        return this == PENDING || this == CONFIRMED;
    }
    
    /**
     * Checks if this status indicates the allocation is complete (final state).
     * 
     * @return true if the allocation is in a final state
     */
    public boolean isFinalState() {
        return this == SHIPPED || this == CANCELLED;
    }
    
    /**
     * Checks if the allocation can be cancelled.
     * 
     * @return true if the allocation can be cancelled
     */
    public boolean canBeCancelled() {
        return this == PENDING || this == CONFIRMED;
    }
    
    /**
     * Checks if the allocation can be confirmed.
     * 
     * @return true if the allocation can be confirmed
     */
    public boolean canBeConfirmed() {
        return this == PENDING;
    }
    
    /**
     * Checks if the allocation can be shipped.
     * 
     * @return true if the allocation can be shipped
     */
    public boolean canBeShipped() {
        return this == CONFIRMED;
    }
    
    /**
     * Gets the next valid status transitions from the current status.
     * 
     * @return array of valid next statuses
     */
    public DomainAllocationStatusEnum[] getValidTransitions() {
        switch (this) {
            case PENDING:
                return new DomainAllocationStatusEnum[]{CONFIRMED, CANCELLED};
            case CONFIRMED:
                return new DomainAllocationStatusEnum[]{SHIPPED, CANCELLED};
            case SHIPPED:
            case CANCELLED:
                return new DomainAllocationStatusEnum[]{}; // Terminal states
            default:
                return new DomainAllocationStatusEnum[]{};
        }
    }
    
    /**
     * Checks if a transition to the target status is valid.
     * 
     * @param targetStatus the target status to transition to
     * @return true if the transition is valid
     */
    public boolean canTransitionTo(DomainAllocationStatusEnum targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        
        DomainAllocationStatusEnum[] validTransitions = getValidTransitions();
        for (DomainAllocationStatusEnum validTransition : validTransitions) {
            if (validTransition == targetStatus) {
                return true;
            }
        }
        return false;
    }
}