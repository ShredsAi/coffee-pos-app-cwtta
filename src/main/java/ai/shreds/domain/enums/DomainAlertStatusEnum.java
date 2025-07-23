package ai.shreds.domain.enums;

/**
 * Enumeration representing the different states of an inventory alert.
 * Tracks the lifecycle of alerts from creation to resolution.
 */
public enum DomainAlertStatusEnum {
    /**
     * Alert is active and requires attention
     */
    ACTIVE,
    
    /**
     * Alert has been acknowledged but not yet resolved
     */
    ACKNOWLEDGED,
    
    /**
     * Alert has been resolved and no longer requires attention
     */
    RESOLVED;
    
    /**
     * Checks if this status indicates the alert still requires attention.
     * 
     * @return true if the alert is active or acknowledged
     */
    public boolean requiresAttention() {
        return this == ACTIVE || this == ACKNOWLEDGED;
    }
    
    /**
     * Checks if this status indicates the alert is resolved.
     * 
     * @return true if the alert is resolved
     */
    public boolean isResolved() {
        return this == RESOLVED;
    }
    
    /**
     * Checks if the alert can be acknowledged.
     * 
     * @return true if the alert can be acknowledged
     */
    public boolean canBeAcknowledged() {
        return this == ACTIVE;
    }
    
    /**
     * Checks if the alert can be resolved.
     * 
     * @return true if the alert can be resolved
     */
    public boolean canBeResolved() {
        return this == ACTIVE || this == ACKNOWLEDGED;
    }
    
    /**
     * Gets the next valid status transitions from the current status.
     * 
     * @return array of valid next statuses
     */
    public DomainAlertStatusEnum[] getValidTransitions() {
        switch (this) {
            case ACTIVE:
                return new DomainAlertStatusEnum[]{ACKNOWLEDGED, RESOLVED};
            case ACKNOWLEDGED:
                return new DomainAlertStatusEnum[]{RESOLVED};
            case RESOLVED:
                return new DomainAlertStatusEnum[]{}; // Terminal state
            default:
                return new DomainAlertStatusEnum[]{};
        }
    }
    
    /**
     * Checks if a transition to the target status is valid.
     * 
     * @param targetStatus the target status to transition to
     * @return true if the transition is valid
     */
    public boolean canTransitionTo(DomainAlertStatusEnum targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        
        DomainAlertStatusEnum[] validTransitions = getValidTransitions();
        for (DomainAlertStatusEnum validTransition : validTransitions) {
            if (validTransition == targetStatus) {
                return true;
            }
        }
        return false;
    }
}