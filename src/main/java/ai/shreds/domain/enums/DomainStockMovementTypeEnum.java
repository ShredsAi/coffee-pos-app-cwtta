package ai.shreds.domain.enums;

public enum DomainStockMovementTypeEnum {
    INBOUND,
    OUTBOUND,
    TRANSFER,
    ADJUSTMENT;
    
    public boolean isPositiveMovement() {
        return this == INBOUND;
    }
    
    public boolean isNegativeMovement() {
        return this == OUTBOUND || this == TRANSFER;
    }
    
    public boolean requiresReason() {
        return this == ADJUSTMENT;
    }
}