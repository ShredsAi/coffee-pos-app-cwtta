package ai.shreds.domain.exceptions;

import ai.shreds.domain.enums.DomainPublicationStatus;

/**
 * Domain Invalid State Transition Exception
 * Thrown when attempting an invalid state transition for product publication status
 */
public class DomainInvalidStateTransitionException extends RuntimeException {
    
    private final DomainPublicationStatus fromStatus;
    private final DomainPublicationStatus toStatus;
    
    /**
     * Constructor with from and to states
     * 
     * @param fromStatus The current publication status
     * @param toStatus The target publication status
     */
    public DomainInvalidStateTransitionException(DomainPublicationStatus fromStatus, DomainPublicationStatus toStatus) {
        super(String.format("Invalid publication status transition from %s to %s", fromStatus, toStatus));
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
    }
    
    /**
     * Gets the current publication status
     * 
     * @return The status trying to transition from
     */
    public DomainPublicationStatus getFromStatus() {
        return fromStatus;
    }
    
    /**
     * Gets the target publication status
     * 
     * @return The status trying to transition to
     */
    public DomainPublicationStatus getToStatus() {
        return toStatus;
    }
    
    /**
     * Gets a human-readable explanation of valid transitions
     * 
     * @return The explanation
     */
    public String getValidTransitionsExplanation() {
        if (fromStatus == null) {
            return "Initial publication status can only be DRAFT or ARCHIVED";
        }
        
        return switch (fromStatus) {
            case DRAFT -> "From DRAFT, you can only transition to REVIEW or remain as DRAFT";
            case REVIEW -> "From REVIEW, you can transition to PUBLISHED, return to DRAFT, or move to ARCHIVED";
            case PUBLISHED -> "From PUBLISHED, you can only transition to ARCHIVED or remain PUBLISHED";
            case ARCHIVED -> "From ARCHIVED, you cannot transition to any other status";
        };
    }
}