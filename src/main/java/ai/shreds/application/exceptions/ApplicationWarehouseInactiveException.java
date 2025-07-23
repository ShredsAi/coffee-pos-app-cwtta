package ai.shreds.application.exceptions;

import java.util.UUID;

public class ApplicationWarehouseInactiveException extends RuntimeException {

    private final UUID warehouseId;

    public ApplicationWarehouseInactiveException(String message, UUID warehouseId) {
        super(message);
        this.warehouseId = warehouseId;
    }

    public UUID getWarehouseId() {
        return warehouseId;
    }
}