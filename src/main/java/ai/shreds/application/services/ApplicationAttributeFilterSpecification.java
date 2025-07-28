package ai.shreds.application.services;

import lombok.Builder;
import lombok.Getter;
import ai.shreds.shared.value_objects.SharedAttributeFilterParams;
import ai.shreds.shared.enums.SharedAttributeType;
import ai.shreds.domain.specifications.DomainAttributeSpecification;
import ai.shreds.domain.enums.DomainAttributeType;

@Getter
@Builder
public class ApplicationAttributeFilterSpecification {
    
    private final Integer page;
    private final Integer size;
    private final String sort;
    private final SharedAttributeType attributeType;
    private final Boolean isFilterable;
    private final Boolean isSearchable;
    private final Boolean isRequired;
    private final Boolean isActive;
    private final String search;
    
    public DomainAttributeSpecification toDomainSpecification() {
        return DomainAttributeSpecification.builder()
            .page(page)
            .size(size)
            .sort(sort)
            .attributeType(attributeType != null ? 
                DomainAttributeType.valueOf(attributeType.name()) : null)
            .isFilterable(isFilterable)
            .isSearchable(isSearchable)
            .isRequired(isRequired)
            .isActive(isActive)
            .search(search)
            .build();
    }
    
    public static ApplicationAttributeFilterSpecification fromFilterParams(SharedAttributeFilterParams params) {
        return ApplicationAttributeFilterSpecification.builder()
            .page(params.getPage() != null ? params.getPage() : 0)
            .size(params.getSize() != null ? params.getSize() : 20)
            .sort(params.getSort())
            .attributeType(params.getAttributeType())
            .isFilterable(params.getIsFilterable())
            .isSearchable(params.getIsSearchable())
            .isRequired(params.getIsRequired())
            .isActive(params.getIsActive())
            .search(params.getSearch())
            .build();
    }
}