package ai.shreds.application.services;

import lombok.Builder;
import lombok.Getter;
import ai.shreds.shared.value_objects.SharedProductFilterParams;
import ai.shreds.shared.enums.SharedPublicationStatus;
import ai.shreds.domain.specifications.DomainProductSpecification;

import java.util.UUID;

@Getter
@Builder
public class ApplicationProductFilterSpecification {
    
    private final Integer page;
    private final Integer size;
    private final String sort;
    private final UUID categoryId;
    private final String brand;
    private final SharedPublicationStatus publicationStatus;
    private final String search;
    private final Boolean isActive;
    
    public DomainProductSpecification toDomainSpecification() {
        return DomainProductSpecification.fromApplicationSpecification(this);
    }
    
    public static ApplicationProductFilterSpecification fromFilterParams(SharedProductFilterParams params) {
        return ApplicationProductFilterSpecification.builder()
            .page(params.getPage() != null ? params.getPage() : 0)
            .size(params.getSize() != null ? params.getSize() : 20)
            .sort(params.getSort())
            .categoryId(params.getCategoryId())
            .brand(params.getBrand())
            .publicationStatus(params.getPublicationStatus())
            .search(params.getSearch())
            .isActive(params.getIsActive())
            .build();
    }
}