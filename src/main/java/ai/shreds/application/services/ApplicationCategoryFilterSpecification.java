package ai.shreds.application.services;

import ai.shreds.domain.specifications.DomainCategorySpecification;
import ai.shreds.shared.value_objects.SharedCategoryFilterParams;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Application layer filter specification for categories.
 */
@Getter
@Builder
public class ApplicationCategoryFilterSpecification {

    private final Integer page;
    private final Integer size;
    private final String sort;
    private final UUID parentCategoryId;
    private final Integer level;
    private final Boolean isActive;
    private final String search;

    /**
     * Converts this application specification to a domain specification.
     */
    public DomainCategorySpecification toDomainSpecification() {
        return DomainCategorySpecification.builder()
            .page(page)
            .size(size)
            .sort(sort)
            .parentCategoryId(parentCategoryId)
            .level(level)
            .isActive(isActive)
            .search(search)
            .build();
    }

    /**
     * Creates an application specification from shared filter parameters.
     */
    public static ApplicationCategoryFilterSpecification fromFilterParams(SharedCategoryFilterParams params) {
        return ApplicationCategoryFilterSpecification.builder()
            .page(params.getPage())
            .size(params.getSize())
            .sort(params.getSort())
            .parentCategoryId(params.getParentCategoryId())
            .level(params.getLevel())
            .isActive(params.getIsActive())
            .search(params.getSearch())
            .build();
    }
}