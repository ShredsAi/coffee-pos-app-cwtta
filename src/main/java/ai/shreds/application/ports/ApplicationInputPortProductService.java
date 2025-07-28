package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedProductDTO;
import ai.shreds.shared.dtos.SharedPagedResponse;
import ai.shreds.shared.dtos.ApplicationCreateProductCommand;
import ai.shreds.shared.dtos.ApplicationUpdateProductCommand;
import ai.shreds.application.services.ApplicationProductFilterSpecification;

import java.util.UUID;

/**
 * Input port for product-related operations in the application layer.
 * Defines the contract for product management use cases.
 */
public interface ApplicationInputPortProductService {
    
    /**
     * Creates a new product based on the provided command.
     * 
     * @param command the command containing product creation data
     * @return the created product as DTO
     */
    SharedProductDTO createProduct(ApplicationCreateProductCommand command);
    
    /**
     * Updates an existing product based on the provided command.
     * 
     * @param command the command containing product update data
     * @return the updated product as DTO
     */
    SharedProductDTO updateProduct(ApplicationUpdateProductCommand command);
    
    /**
     * Retrieves a product by its unique identifier.
     * 
     * @param id the product identifier
     * @return the product as DTO
     */
    SharedProductDTO getProduct(UUID id);
    
    /**
     * Lists products based on the provided filter specification.
     * 
     * @param specification the filter and pagination specification
     * @return paged response containing matching products
     */
    SharedPagedResponse<SharedProductDTO> listProducts(ApplicationProductFilterSpecification specification);
    
    /**
     * Deletes a product by its unique identifier.
     * 
     * @param id the product identifier to delete
     */
    void deleteProduct(UUID id);
}