package ai.shreds.domain.specifications;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Specification for querying categories in the domain layer.
 */
@Getter
@Builder
public class DomainCategorySpecification {
    private final Integer page;
    private final Integer size;
    private final String sort;
    private final UUID parentCategoryId;
    private final Integer level;
    private final Boolean isActive;
    private final String search;
    
    /**
     * Gets the effective page number, defaulting to 0 if null
     */
    public Integer getEffectivePage() {
        return page != null ? page : 0;
    }
    
    /**
     * Gets the effective page size, defaulting to 20 if null
     */
    public Integer getEffectiveSize() {
        return size != null ? size : 20;
    }
}