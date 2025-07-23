package ai.shreds.domain.commands;

import ai.shreds.domain.value_objects.DomainProductIdValue;
import ai.shreds.domain.value_objects.DomainQuantityValue;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Command object for creating a new batch.
 * Contains all the data needed to create a batch entity.
 */
public class DomainCreateBatchCommand {
    private final UUID warehouseId;
    private final DomainProductIdValue productId;
    private final String batchNumber;
    private final DomainQuantityValue quantity;
    private final LocalDate manufacturingDate;
    private final LocalDate expirationDate;
    private final UUID supplierId;

    /**
     * Constructs a new batch creation command.
     *
     * @param warehouseId the warehouse ID
     * @param productId the product ID
     * @param batchNumber the batch number
     * @param quantity the batch quantity
     * @param manufacturingDate the manufacturing date (optional)
     * @param expirationDate the expiration date (optional)
     * @param supplierId the supplier ID (optional)
     */
    public DomainCreateBatchCommand(
            UUID warehouseId,
            DomainProductIdValue productId,
            String batchNumber,
            DomainQuantityValue quantity,
            LocalDate manufacturingDate,
            LocalDate expirationDate,
            UUID supplierId) {
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.manufacturingDate = manufacturingDate;
        this.expirationDate = expirationDate;
        this.supplierId = supplierId;
    }

    /**
     * Gets the warehouse ID.
     *
     * @return the warehouseId
     */
    public UUID getWarehouseId() {
        return warehouseId;
    }

    /**
     * Gets the product ID.
     *
     * @return the productId
     */
    public DomainProductIdValue getProductId() {
        return productId;
    }

    /**
     * Gets the batch number.
     *
     * @return the batchNumber
     */
    public String getBatchNumber() {
        return batchNumber;
    }

    /**
     * Gets the batch quantity.
     *
     * @return the quantity
     */
    public DomainQuantityValue getQuantity() {
        return quantity;
    }

    /**
     * Gets the manufacturing date.
     *
     * @return the manufacturingDate (may be null)
     */
    public LocalDate getManufacturingDate() {
        return manufacturingDate;
    }

    /**
     * Gets the expiration date.
     *
     * @return the expirationDate (may be null)
     */
    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    /**
     * Gets the supplier ID.
     *
     * @return the supplierId (may be null)
     */
    public UUID getSupplierId() {
        return supplierId;
    }

    @Override
    public String toString() {
        return "DomainCreateBatchCommand{" +
                "warehouseId=" + warehouseId +
                ", productId=" + productId +
                ", batchNumber='" + batchNumber + '\'' +
                ", quantity=" + quantity +
                ", expirationDate=" + expirationDate +
                '}';
    }
}