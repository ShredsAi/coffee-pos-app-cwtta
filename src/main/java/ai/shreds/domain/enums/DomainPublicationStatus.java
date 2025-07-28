package ai.shreds.domain.enums;

/**
 * Domain Publication Status Enum
 * Represents the publication workflow states for products
 */
public enum DomainPublicationStatus {
    DRAFT,
    REVIEW,
    PUBLISHED,
    ARCHIVED;
    
    /**
     * Checks if transition from current status to target status is allowed
     * Business Rule: DRAFT → REVIEW → PUBLISHED → ARCHIVED (no backward skip except to ARCHIVED)
     */
    public boolean canTransitionTo(DomainPublicationStatus target) {
        if (target == null) {
            return false;
        }
        
        // Can always transition to ARCHIVED from any state
        if (target == ARCHIVED) {
            return true;
        }
        
        // Can't transition from ARCHIVED to any other state
        if (this == ARCHIVED && target != ARCHIVED) {
            return false;
        }
        
        switch (this) {
            case DRAFT:
                return target == REVIEW || target == DRAFT;
            case REVIEW:
                return target == PUBLISHED || target == DRAFT || target == REVIEW;
            case PUBLISHED:
                return target == PUBLISHED; // Can stay published
            case ARCHIVED:
                return target == ARCHIVED; // Already handled above
            default:
                return false;
        }
    }
    
    /**
     * Checks if this status allows product modifications
     */
    public boolean allowsModification() {
        return this == DRAFT || this == REVIEW;
    }
    
    /**
     * Checks if this status is publicly visible
     */
    public boolean isPubliclyVisible() {
        return this == PUBLISHED;
    }
    
    /**
     * Checks if this status requires validation
     */
    public boolean requiresValidation() {
        return this == REVIEW || this == PUBLISHED;
    }
}