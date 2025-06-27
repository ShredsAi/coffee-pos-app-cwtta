package ai.shreds.domain.entities;

import ai.shreds.domain.enums.DomainAlertSeverityEnum;
import ai.shreds.domain.enums.DomainAlertStatusEnum;
import ai.shreds.domain.exceptions.DomainValidationException;
import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.value_objects.DomainQuantityValue;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a low stock alert.
 * Generated when inventory levels fall below safety stock or reorder point thresholds.
 * Tracks acknowledgment and resolution of stock level issues.
 */
public class DomainLowStockAlertEntity {
    private final UUID id;
    private final UUID warehouseId;
    private final DomainProductIdValue productId;
    private final DomainQuantityValue currentQuantity;
    private final DomainQuantityValue safetyStockLevel;
    private final DomainAlertSeverityEnum severity;
    private DomainAlertStatusEnum status;
    private final LocalDateTime createdAt;
    private LocalDateTime acknowledgedAt;
    private String acknowledgedBy;
    private LocalDateTime resolvedAt;

    /**
     * Constructs a new low stock alert entity.
     *
     * @param id the alert ID
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param currentQuantity the current stock quantity when alert was created
     * @param safetyStockLevel the safety stock threshold that was breached
     * @param severity the severity level of the alert
     * @param status the current status of the alert
     * @param createdAt when the alert was created
     * @param acknowledgedAt when the alert was acknowledged (may be null)
     * @param acknowledgedBy who acknowledged the alert (may be null)
     * @param resolvedAt when the alert was resolved (may be null)
     */
    public DomainLowStockAlertEntity(
            UUID id,
            UUID warehouseId,
            DomainProductIdValue productId,
            DomainQuantityValue currentQuantity,
            DomainQuantityValue safetyStockLevel,
            DomainAlertSeverityEnum severity,
            DomainAlertStatusEnum status,
            LocalDateTime createdAt,
            LocalDateTime acknowledgedAt,
            String acknowledgedBy,
            LocalDateTime resolvedAt) {
        
        validateConstructorParameters(id, warehouseId, productId, currentQuantity, 
                safetyStockLevel, severity, status, createdAt);
        
        this.id = id;
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.currentQuantity = currentQuantity;
        this.safetyStockLevel = safetyStockLevel;
        this.severity = severity;
        this.status = status;
        this.createdAt = createdAt;
        this.acknowledgedAt = acknowledgedAt;
        this.acknowledgedBy = acknowledgedBy;
        this.resolvedAt = resolvedAt;
    }
    
    private void validateConstructorParameters(
            UUID id, UUID warehouseId, DomainProductIdValue productId, 
            DomainQuantityValue currentQuantity, DomainQuantityValue safetyStockLevel, 
            DomainAlertSeverityEnum severity, DomainAlertStatusEnum status, 
            LocalDateTime createdAt) {
        
        if (id == null) {
            throw new DomainValidationException("Alert ID cannot be null", "id", null);
        }
        
        if (warehouseId == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "warehouseId", null);
        }
        
        if (productId == null) {
            throw new DomainValidationException("Product ID cannot be null", "productId", null);
        }
        
        if (currentQuantity == null) {
            throw new DomainValidationException("Current quantity cannot be null", "currentQuantity", null);
        }
        
        if (safetyStockLevel == null) {
            throw new DomainValidationException("Safety stock level cannot be null", "safetyStockLevel", null);
        }
        
        if (severity == null) {
            throw new DomainValidationException("Severity cannot be null", "severity", null);
        }
        
        if (status == null) {
            throw new DomainValidationException("Status cannot be null", "status", null);
        }
        
        if (createdAt == null) {
            throw new DomainValidationException("Created at cannot be null", "createdAt", null);
        }
        
        if (createdAt.isAfter(LocalDateTime.now())) {
            throw new DomainValidationException(
                "Created at cannot be in the future", 
                "createdAt", 
                createdAt
            );
        }
        
        // Validate that current quantity is actually below safety stock level
        if (!currentQuantity.getUnit().equals(safetyStockLevel.getUnit())) {
            throw new DomainValidationException(
                "Current quantity and safety stock level must have the same unit",
                "unit",
                currentQuantity.getUnit() + " vs " + safetyStockLevel.getUnit()
            );
        }
        
        if (currentQuantity.isGreaterThan(safetyStockLevel)) {
            throw new DomainValidationException(
                "Cannot create low stock alert when current quantity is above safety stock level",
                "currentQuantity",
                currentQuantity
            );
        }
    }
    
    /**
     * Factory method to create a new active low stock alert.
     *
     * @param id the alert ID
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param currentQuantity the current stock quantity
     * @param safetyStockLevel the safety stock threshold
     * @return a new active low stock alert
     */
    public static DomainLowStockAlertEntity createNew(
            UUID id,
            UUID warehouseId,
            DomainProductIdValue productId,
            DomainQuantityValue currentQuantity,
            DomainQuantityValue safetyStockLevel) {
        
        // Calculate severity based on how far below safety stock we are
        DomainAlertSeverityEnum severity = calculateSeverity(currentQuantity, safetyStockLevel);
        
        return new DomainLowStockAlertEntity(
                id,
                warehouseId,
                productId,
                currentQuantity,
                safetyStockLevel,
                severity,
                DomainAlertStatusEnum.ACTIVE,
                LocalDateTime.now(),
                null, // Not acknowledged yet
                null, // Not acknowledged by anyone yet
                null  // Not resolved yet
        );
    }
    
    private static DomainAlertSeverityEnum calculateSeverity(
            DomainQuantityValue currentQuantity, 
            DomainQuantityValue safetyStockLevel) {
        
        // Calculate percentage of safety stock level
        double percentageOfSafetyStock = currentQuantity.getValue()
                .divide(safetyStockLevel.getValue(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(java.math.BigDecimal.valueOf(100))
                .doubleValue();
        
        return DomainAlertSeverityEnum.fromStockLevelPercentage((int) percentageOfSafetyStock);
    }

    /**
     * Acknowledges the alert, indicating that someone is aware and working on it.
     *
     * @param userId the ID of the user acknowledging the alert
     */
    public void acknowledge(String userId) {
        if (!status.canBeAcknowledged()) {
            throw new DomainValidationException(
                String.format("Cannot acknowledge alert with status %s", status),
                "status",
                status
            );
        }
        
        if (userId == null || userId.trim().isEmpty()) {
            throw new DomainValidationException("User ID cannot be null or empty", "userId", userId);
        }
        
        this.status = DomainAlertStatusEnum.ACKNOWLEDGED;
        this.acknowledgedAt = LocalDateTime.now();
        this.acknowledgedBy = userId.trim();
    }

    /**
     * Resolves the alert, indicating that the stock level issue has been addressed.
     */
    public void resolve() {
        if (!status.canBeResolved()) {
            throw new DomainValidationException(
                String.format("Cannot resolve alert with status %s", status),
                "status",
                status
            );
        }
        
        this.status = DomainAlertStatusEnum.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
    }

    /**
     * Checks if the alert is currently active and requiring attention.
     *
     * @return true if the alert is active or acknowledged
     */
    public boolean isActive() {
        return status.requiresAttention();
    }
    
    /**
     * Checks if the alert has been resolved.
     *
     * @return true if the alert status is resolved
     */
    public boolean isResolved() {
        return status.isResolved();
    }
    
    /**
     * Calculates how long the alert has been unresolved in hours.
     *
     * @return hours since the alert was created, or null if resolved
     */
    public Long getUnresolvedHours() {
        if (isResolved()) {
            return null;
        }
        
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toHours();
    }
    
    /**
     * Checks if the alert is overdue based on the severity level.
     *
     * @return true if the alert has been unresolved longer than recommended
     */
    public boolean isOverdue() {
        Long unresolvedHours = getUnresolvedHours();
        if (unresolvedHours == null) {
            return false; // Resolved alerts are not overdue
        }
        
        return unresolvedHours > severity.getRecommendedMaxHoursToResolve();
    }
    
    /**
     * Gets the shortage amount (how much below safety stock level).
     *
     * @return the shortage quantity
     */
    public DomainQuantityValue getShortageAmount() {
        return safetyStockLevel.subtract(currentQuantity);
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getWarehouseId() {
        return warehouseId;
    }

    public DomainProductIdValue getProductId() {
        return productId;
    }

    public DomainQuantityValue getCurrentQuantity() {
        return currentQuantity;
    }

    public DomainQuantityValue getSafetyStockLevel() {
        return safetyStockLevel;
    }

    public DomainAlertSeverityEnum getSeverity() {
        return severity;
    }

    public DomainAlertStatusEnum getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        DomainLowStockAlertEntity that = (DomainLowStockAlertEntity) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "DomainLowStockAlertEntity{" +
                "id=" + id +
                ", warehouseId=" + warehouseId +
                ", productId=" + productId +
                ", currentQuantity=" + currentQuantity +
                ", safetyStockLevel=" + safetyStockLevel +
                ", severity=" + severity +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}