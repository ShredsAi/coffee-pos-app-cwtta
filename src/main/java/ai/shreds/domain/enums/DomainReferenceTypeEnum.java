package ai.shreds.domain.enums;

/**
 * Enumeration representing different types of references that can be associated
 * with stock movements for audit and traceability purposes.
 */
public enum DomainReferenceTypeEnum {
    PURCHASE_ORDER,
    SALES_ORDER,
    TRANSFER_ORDER,
    RETURN_ORDER,
    ADJUSTMENT_ORDER;
    
    /**
     * Determines if this reference type typically results in inbound stock movement
     * @return true if this reference type usually increases stock levels
     */
    public boolean isInboundType() {
        return this == PURCHASE_ORDER || this == RETURN_ORDER;
    }
    
    /**
     * Determines if this reference type typically results in outbound stock movement
     * @return true if this reference type usually decreases stock levels
     */
    public boolean isOutboundType() {
        return this == SALES_ORDER;
    }
    
    /**
     * Determines if this reference type requires additional approval workflow
     * @return true if movements with this reference type need approval
     */
    public boolean requiresApproval() {
        return this == ADJUSTMENT_ORDER;
    }
}