package ai.shreds.domain.ports;

import java.util.List;
import java.util.Map;

/**
 * Output port for user service operations.
 * Defines the contract for integration with external user management service.
 * To be implemented by infrastructure layer.
 * 
 * This port allows the domain to validate users and retrieve user information
 * without depending on external service implementation details.
 */
public interface DomainOutputPortUserService {
    
    /**
     * Validates that a user exists and is authorized to perform inventory operations.
     * Should check both existence and authorization status of the user.
     * 
     * @param userId the user ID to validate
     * @return true if the user exists and is authorized for inventory operations
     * @throws ai.shreds.domain.exceptions.DomainValidationException if userId is null or empty
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    boolean validateUser(String userId);
    
    /**
     * Gets comprehensive user details from the user management service.
     * Returns essential user information needed for audit and authorization.
     * 
     * @param userId the user ID
     * @return map of user details including name, role, permissions, department, etc.
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if user not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    Map<String, Object> getUserDetails(String userId);
    
    /**
     * Gets the user's display name for audit logs and reporting.
     * 
     * @param userId the user ID
     * @return the user's full name or display name
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if user not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    String getUserDisplayName(String userId);
    
    /**
     * Gets the user's role for authorization purposes.
     * 
     * @param userId the user ID
     * @return the user's primary role (e.g., "WAREHOUSE_OPERATOR", "WAREHOUSE_MANAGER")
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if user not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    String getUserRole(String userId);
    
    /**
     * Checks if a user has permission to perform specific operations.
     * 
     * @param userId the user ID
     * @param permission the permission to check (e.g., "STOCK_MOVEMENT", "INVENTORY_ADJUSTMENT")
     * @return true if the user has the specified permission
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if user not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    boolean hasPermission(String userId, String permission);
    
    /**
     * Checks if a user can access a specific warehouse.
     * Used for warehouse-level access control.
     * 
     * @param userId the user ID
     * @param warehouseId the warehouse ID
     * @return true if the user can access the warehouse
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if user not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    boolean canAccessWarehouse(String userId, String warehouseId);
    
    /**
     * Gets all permissions for a user.
     * 
     * @param userId the user ID
     * @return list of permissions the user has
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if user not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    List<String> getUserPermissions(String userId);
    
    /**
     * Gets warehouses that a user can access.
     * 
     * @param userId the user ID
     * @return list of warehouse IDs the user can access
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if user not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    List<String> getAccessibleWarehouses(String userId);
    
    /**
     * Validates multiple users in a single call for better performance.
     * 
     * @param userIds the list of user IDs to validate
     * @return map of user ID to validation result (true if valid)
     * @throws ai.shreds.domain.exceptions.DomainValidationException if userIds list is null or empty
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    Map<String, Boolean> validateMultipleUsers(List<String> userIds);
    
    /**
     * Gets basic user information for multiple users.
     * Efficient for bulk operations and audit reporting.
     * 
     * @param userIds the list of user IDs
     * @return map of user ID to basic user information
     * @throws ai.shreds.domain.exceptions.DomainValidationException if userIds list is null or empty
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    Map<String, Map<String, Object>> getMultipleUserDetails(List<String> userIds);
    
    /**
     * Checks if a user account is active and not locked or suspended.
     * 
     * @param userId the user ID
     * @return true if the user account is active
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if user not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    boolean isUserActive(String userId);
    
    /**
     * Gets the user's department or organization unit.
     * Used for reporting and access control.
     * 
     * @param userId the user ID
     * @return the user's department or organization unit
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if user not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    String getUserDepartment(String userId);
    
    /**
     * Logs a user action for audit purposes.
     * Records user activities for compliance and security monitoring.
     * 
     * @param userId the user ID
     * @param action the action performed
     * @param details additional details about the action
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if logging fails
     */
    void logUserAction(String userId, String action, Map<String, Object> details);
    
    /**
     * Checks if the external user service is available.
     * Used for health checks and graceful degradation.
     * 
     * @return true if the service is available and responding
     */
    boolean isServiceAvailable();
    
    /**
     * Gets the service health status with additional details.
     * 
     * @return map containing service health information
     */
    Map<String, Object> getServiceHealthStatus();
    
    /**
     * Validates that a user can perform a specific stock movement operation.
     * Combines user validation, permission check, and warehouse access.
     * 
     * @param userId the user ID
     * @param warehouseId the warehouse ID
     * @param operationType the type of operation (e.g., "INBOUND", "OUTBOUND", "ADJUSTMENT")
     * @return true if the user is authorized for the operation
     * @throws ai.shreds.domain.exceptions.DomainEntityNotFoundException if user not found
     * @throws ai.shreds.infrastructure.exceptions.InfrastructureExternalServiceException if service call fails
     */
    boolean canPerformStockOperation(String userId, String warehouseId, String operationType);
}