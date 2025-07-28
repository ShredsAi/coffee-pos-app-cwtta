package ai.shreds.shared.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * An enumeration representing the publication status of a product.
 * Used to track the product's lifecycle state from draft to archived.
 */
public enum SharedPublicationStatus {
    /**
     * Initial state for products that are being created or edited 
     * but not ready for review.
     */
    DRAFT("Draft", "Content is being created or edited", true, false),
    
    /**
     * Product is complete and ready for review before publication.
     */
    REVIEW("Under Review", "Product is being reviewed for publication", false, false),
    
    /**
     * Product is approved and visible to customers.
     */
    PUBLISHED("Published", "Product is live and available to customers", false, true),
    
    /**
     * Product is no longer actively sold but maintained for historical reference.
     */
    ARCHIVED("Archived", "Product is archived and no longer active", false, false);

    private final String displayName;
    private final String description;
    private final boolean editable;
    private final boolean publiclyVisible;

    SharedPublicationStatus(String displayName, String description, boolean editable, boolean publiclyVisible) {
        this.displayName = displayName;
        this.description = description;
        this.editable = editable;
        this.publiclyVisible = publiclyVisible;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isPubliclyVisible() {
        return publiclyVisible;
    }

    public boolean isActive() {
        return this != ARCHIVED;
    }

    public boolean isDraft() {
        return this == DRAFT;
    }

    public boolean isPublished() {
        return this == PUBLISHED;
    }

    public boolean canTransitionTo(SharedPublicationStatus target) {
        if (target == null || target == this) {
            return false;
        }

        switch (this) {
            case DRAFT:
                return EnumSet.of(REVIEW, ARCHIVED).contains(target);
            case REVIEW:
                return EnumSet.of(PUBLISHED, DRAFT, ARCHIVED).contains(target);
            case PUBLISHED:
                return EnumSet.of(ARCHIVED, DRAFT).contains(target);
            case ARCHIVED:
                return target == DRAFT; // Can only restore to draft
            default:
                return false;
        }
    }

    public Set<SharedPublicationStatus> getAllowedTransitions() {
        return EnumSet.allOf(SharedPublicationStatus.class)
                .stream()
                .filter(this::canTransitionTo)
                .collect(() -> EnumSet.noneOf(SharedPublicationStatus.class),
                        Set::add, Set::addAll);
    }

    public boolean requiresReview() {
        return this == REVIEW;
    }

    public static Set<SharedPublicationStatus> getPublicStatuses() {
        return EnumSet.of(PUBLISHED);
    }

    public static Set<SharedPublicationStatus> getEditableStatuses() {
        return EnumSet.allOf(SharedPublicationStatus.class)
                .stream()
                .filter(SharedPublicationStatus::isEditable)
                .collect(() -> EnumSet.noneOf(SharedPublicationStatus.class),
                        Set::add, Set::addAll);
    }

    public static Set<SharedPublicationStatus> getActiveStatuses() {
        return EnumSet.allOf(SharedPublicationStatus.class)
                .stream()
                .filter(SharedPublicationStatus::isActive)
                .collect(() -> EnumSet.noneOf(SharedPublicationStatus.class),
                        Set::add, Set::addAll);
    }

    public static SharedPublicationStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        
        try {
            return valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid publication status: " + status);
        }
    }

    /**
     * Get the next logical status in the workflow
     */
    public SharedPublicationStatus getNextStatus() {
        switch (this) {
            case DRAFT:
                return REVIEW;
            case REVIEW:
                return PUBLISHED;
            case PUBLISHED:
                return ARCHIVED;
            case ARCHIVED:
            default:
                return null;
        }
    }

    /**
     * Get the previous status in the workflow for rollback
     */
    public SharedPublicationStatus getPreviousStatus() {
        switch (this) {
            case REVIEW:
                return DRAFT;
            case PUBLISHED:
                return REVIEW;
            case ARCHIVED:
                return PUBLISHED;
            case DRAFT:
            default:
                return null;
        }
    }
}