package ai.shreds.shared.enums;

import java.util.EnumSet;
import java.util.Set;

public enum DomainPublicationStatus {
    DRAFT,
    REVIEW,
    PUBLISHED,
    ARCHIVED;

    public boolean canTransitionTo(DomainPublicationStatus target) {
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
                return target == DRAFT;
            default:
                return false;
        }
    }
}