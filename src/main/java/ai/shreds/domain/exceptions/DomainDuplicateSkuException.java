package ai.shreds.domain.exceptions;

/**
 * Domain Duplicate SKU Exception
 * Thrown when attempting to create or update a product with a SKU that already exists
 */
public class DomainDuplicateSkuException extends RuntimeException {
    
    private final String sku;
    
    /**
     * Constructor with SKU
     * 
     * @param sku The duplicate SKU
     */
    public DomainDuplicateSkuException(String sku) {
        super(String.format("Product with SKU '%s' already exists", sku));
        this.sku = sku;
    }
    
    /**
     * Gets the duplicate SKU
     * 
     * @return The SKU that caused the conflict
     */
    public String getSku() {
        return sku;
    }
}