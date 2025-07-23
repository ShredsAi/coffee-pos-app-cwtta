package ai.shreds.domain.entities;

import ai.shreds.domain.value_objects.DomainAddressValue;
import ai.shreds.domain.exceptions.DomainValidationException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Domain entity representing a warehouse facility where inventory is stored and managed.
 * Encapsulates business rules related to warehouse operations and status management.
 */
public class DomainWarehouseEntity {
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9]{3,10}$");
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_CODE_LENGTH = 10;
    
    private final UUID id;
    private final String code;
    private String name;
    private DomainAddressValue address;
    private boolean isActive;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DomainWarehouseEntity(
            UUID id,
            String code,
            String name,
            DomainAddressValue address,
            boolean isActive,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        validateConstructorParameters(id, code, name, address, createdAt);
        
        this.id = id;
        this.code = code.toUpperCase().trim();
        this.name = name.trim();
        this.address = address;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt != null ? updatedAt : createdAt;
    }
    
    private void validateConstructorParameters(UUID id, String code, String name, DomainAddressValue address, LocalDateTime createdAt) {
        if (id == null) {
            throw new DomainValidationException("Warehouse ID cannot be null", "id", null);
        }
        
        validateCode(code);
        validateName(name);
        
        if (address == null) {
            throw new DomainValidationException("Warehouse address cannot be null", "address", null);
        }
        
        if (createdAt == null) {
            throw new DomainValidationException("Created date cannot be null", "createdAt", null);
        }
        
        if (createdAt.isAfter(LocalDateTime.now())) {
            throw new DomainValidationException("Created date cannot be in the future", "createdAt", createdAt);
        }
    }
    
    private void validateCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new DomainValidationException("Warehouse code is required", "code", code);
        }
        
        String trimmedCode = code.trim();
        if (trimmedCode.length() > MAX_CODE_LENGTH) {
            throw new DomainValidationException(
                String.format("Warehouse code cannot exceed %d characters", MAX_CODE_LENGTH), 
                "code", 
                code
            );
        }
        
        if (!CODE_PATTERN.matcher(trimmedCode).matches()) {
            throw new DomainValidationException(
                "Warehouse code must contain only uppercase letters and numbers", 
                "code", 
                code
            );
        }
    }
    
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new DomainValidationException("Warehouse name is required", "name", name);
        }
        
        if (name.trim().length() > MAX_NAME_LENGTH) {
            throw new DomainValidationException(
                String.format("Warehouse name cannot exceed %d characters", MAX_NAME_LENGTH), 
                "name", 
                name
            );
        }
    }

    /**
     * Checks if the warehouse is operational and can perform inventory operations
     * @return true if the warehouse is active
     */
    public boolean isOperational() {
        return isActive;
    }

    /**
     * Checks if the warehouse can accept incoming stock
     * @return true if the warehouse is active
     */
    public boolean canAcceptStock() {
        return isActive;
    }
    
    /**
     * Checks if the warehouse can process outbound shipments
     * @return true if the warehouse is active
     */
    public boolean canShipStock() {
        return isActive;
    }
    
    /**
     * Validates if stock operations are allowed in this warehouse
     * @throws DomainValidationException if warehouse is inactive
     */
    public void validateStockOperationsAllowed() {
        if (!isActive) {
            throw new DomainValidationException(
                "Stock operations are not allowed in inactive warehouse", 
                "isActive", 
                isActive
            );
        }
    }

    /**
     * Deactivates the warehouse, preventing further stock operations
     * @param updatedAt the timestamp when deactivation occurs
     */
    public void deactivate(LocalDateTime updatedAt) {
        if (!isActive) {
            throw new DomainValidationException(
                "Warehouse is already inactive", 
                "isActive", 
                isActive
            );
        }
        
        if (updatedAt == null) {
            throw new DomainValidationException("Updated timestamp cannot be null", "updatedAt", null);
        }
        
        this.isActive = false;
        this.updatedAt = updatedAt;
    }

    /**
     * Activates the warehouse, enabling stock operations
     * @param updatedAt the timestamp when activation occurs
     */
    public void activate(LocalDateTime updatedAt) {
        if (isActive) {
            throw new DomainValidationException(
                "Warehouse is already active", 
                "isActive", 
                isActive
            );
        }
        
        if (updatedAt == null) {
            throw new DomainValidationException("Updated timestamp cannot be null", "updatedAt", null);
        }
        
        this.isActive = true;
        this.updatedAt = updatedAt;
    }

    /**
     * Updates the warehouse address
     * @param newAddress the new address
     * @param updatedAt the timestamp when update occurs
     */
    public void updateAddress(DomainAddressValue newAddress, LocalDateTime updatedAt) {
        if (newAddress == null) {
            throw new DomainValidationException("Address cannot be null", "address", null);
        }
        
        if (updatedAt == null) {
            throw new DomainValidationException("Updated timestamp cannot be null", "updatedAt", null);
        }
        
        if (this.address.equals(newAddress)) {
            return; // No change needed
        }
        
        this.address = newAddress;
        this.updatedAt = updatedAt;
    }
    
    /**
     * Updates the warehouse name
     * @param newName the new name
     * @param updatedAt the timestamp when update occurs
     */
    public void updateName(String newName, LocalDateTime updatedAt) {
        validateName(newName);
        
        if (updatedAt == null) {
            throw new DomainValidationException("Updated timestamp cannot be null", "updatedAt", null);
        }
        
        String trimmedNewName = newName.trim();
        if (this.name.equals(trimmedNewName)) {
            return; // No change needed
        }
        
        this.name = trimmedNewName;
        this.updatedAt = updatedAt;
    }
    
    /**
     * Checks if the warehouse status has changed
     * @param newStatus the new status to compare
     * @return true if status is different
     */
    public boolean hasStatusChanged(boolean newStatus) {
        return this.isActive != newStatus;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public DomainAddressValue getAddress() {
        return address;
    }

    public boolean isActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        DomainWarehouseEntity that = (DomainWarehouseEntity) o;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return "DomainWarehouseEntity{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}