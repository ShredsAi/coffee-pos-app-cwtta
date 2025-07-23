package ai.shreds.domain.enums;

/**
 * Enumeration representing the different states of a stock reservation.
 * Reservations can be active, expired, confirmed (converted to allocation), or cancelled.
 */
public enum DomainReservationStatusEnum {
    /**
     * Reservation is active and holding stock
     */
    ACTIVE,
    
    /**
     * Reservation has expired and stock is released
     */
    EXPIRED,
    
    /**
     * Reservation has been confirmed and converted to an allocation
     */
    CONFIRMED,
    
    /**
     * Reservation has been cancelled and stock is released
     */
    CANCELLED;
    
    /**
     * Checks if this status indicates the reservation is still holding stock.
     * 
     * @return true if the reservation is actively holding stock
     */
    public boolean isHoldingStock() {
        return this == ACTIVE;
    }
    
    /**
     * Checks if this status indicates the reservation is no longer active.
     * 
     * @return true if the reservation is no longer holding stock
     */
    public boolean isInactive() {
        return this == EXPIRED || this == CONFIRMED || this == CANCELLED;
    }
    
    /**
     * Checks if the reservation can be cancelled.
     * 
     * @return true if the reservation can be cancelled
     */
    public boolean canBeCancelled() {
        return this == ACTIVE;
    }
    
    /**
     * Checks if the reservation can be confirmed.
     * 
     * @return true if the reservation can be confirmed
     */
    public boolean canBeConfirmed() {
        return this == ACTIVE;
    }
    
    /**
     * Gets the next valid status transitions from the current status.
     * 
     * @return array of valid next statuses
     */
    public DomainReservationStatusEnum[] getValidTransitions() {
        switch (this) {
            case ACTIVE:
                return new DomainReservationStatusEnum[]{EXPIRED, CONFIRMED, CANCELLED};
            case EXPIRED:
            case CONFIRMED:
            case CANCELLED:
                return new DomainReservationStatusEnum[]{}; // Terminal states
            default:
                return new DomainReservationStatusEnum[]{};
        }
    }
    
    /**
     * Checks if a transition to the target status is valid.
     * 
     * @param targetStatus the target status to transition to
     * @return true if the transition is valid
     */
    public boolean canTransitionTo(DomainReservationStatusEnum targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        
        DomainReservationStatusEnum[] validTransitions = getValidTransitions();
        for (DomainReservationStatusEnum validTransition : validTransitions) {
            if (validTransition == targetStatus) {
                return true;
            }
        }
        return false;
    }
}