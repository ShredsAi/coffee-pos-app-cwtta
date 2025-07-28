package ai.shreds.infrastructure.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;
import java.time.Instant;

/**
 * JPA entity representing a product category in the database.
 * Contains hierarchical relationship (self-referencing) for category tree structure.
 */
@Entity
@Table(name = "category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"parent", "children"})
@EqualsAndHashCode(exclude = {"parent", "children"})
@Builder
public class InfrastructureCategoryJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "parent_category_id")
    private UUID parentCategoryId;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false)
    private String path;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id", insertable = false, updatable = false)
    private InfrastructureCategoryJpaEntity parent;

    @OneToMany(mappedBy = "parent", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("sortOrder ASC")
    private List<InfrastructureCategoryJpaEntity> children = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Version
    private Long version;

    /**
     * Adds a child category to this category
     * @param child The child category to add
     */
    public void addChild(InfrastructureCategoryJpaEntity child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
        child.setParent(this);
        child.setParentCategoryId(this.id);
    }

    /**
     * Pre-persist hook to set default values
     */
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
        if (isActive == null) {
            isActive = true;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }

    /**
     * Pre-update hook to update the updatedAt timestamp
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
