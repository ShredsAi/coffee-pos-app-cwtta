package ai.shreds.shared.dtos;

import ai.shreds.domain.entities.DomainCategoryEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SharedCategoryDTO {

    private UUID id;

    @NotBlank(message = "Category name is required")
    @Size(max = 255, message = "Category name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 255, message = "Slug must not exceed 255 characters")
    private String slug;

    private UUID parentCategoryId;

    @Min(value = 0, message = "Level must be non-negative")
    @Builder.Default
    private Integer level = 0;

    @Size(max = 1000, message = "Path must not exceed 1000 characters")
    private String path;

    @Min(value = 0, message = "Sort order must be non-negative")
    @Builder.Default
    private Integer sortOrder = 0;

    @Builder.Default
    private Boolean isActive = true;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;

    private Long version;

    /**
     * Converts this DTO into a DomainCategoryEntity.
     *
     * @return DomainCategoryEntity populated with values from this DTO.
     */
    public DomainCategoryEntity toEntity() {
        return DomainCategoryEntity.fromDTO(this);
    }

    /**
     * Creates a DTO from a DomainCategoryEntity.
     *
     * @param entity the domain entity to convert – must not be null.
     * @return SharedCategoryDTO containing copied values from entity.
     */
    public static SharedCategoryDTO fromEntity(DomainCategoryEntity entity) {
        if (entity == null) {
            return null;
        }

        return SharedCategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .slug(entity.getSlug())
                .parentCategoryId(entity.getParentCategoryId())
                .level(entity.getLevel())
                .path(entity.getPath())
                .sortOrder(entity.getSortOrder())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .version(entity.getVersion())
                .build();
    }
}
