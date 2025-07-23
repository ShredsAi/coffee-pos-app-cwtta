package ai.shreds.shared.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ai.shreds.shared.value_objects.SharedAddressValue;

/**
 * DTO for creating or updating a warehouse.
 * Contains warehouse metadata and location information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedWarehouseRequestDTO {

    @NotBlank(message = "Warehouse code must not be blank")
    @Size(min = 3, max = 10, message = "Warehouse code must be between 3 and 10 characters")
    @Pattern(regexp = "^[A-Z0-9]{3,10}$", message = "Warehouse code must contain only uppercase letters and numbers")
    private String code;

    @NotBlank(message = "Warehouse name must not be blank")
    @Size(max = 100, message = "Warehouse name must be at most 100 characters")
    private String name;

    @NotNull(message = "Warehouse address must not be null")
    @Valid
    private SharedAddressValue address;

    @NotNull(message = "Warehouse active status must not be null")
    private Boolean isActive;
    
    /**
     * Builder method to create a warehouse request with default active status.
     * By default, warehouses are created in active state unless explicitly set otherwise.
     *
     * @param code Unique warehouse code
     * @param name Warehouse name
     * @param address Warehouse physical address
     * @return A new warehouse request with default active status
     */
    public static SharedWarehouseRequestDTO createActive(String code, String name, SharedAddressValue address) {
        return SharedWarehouseRequestDTO.builder()
            .code(code)
            .name(name)
            .address(address)
            .isActive(true)
            .build();
    }
    
    /**
     * Builder method to create an inactive warehouse request.
     *
     * @param code Unique warehouse code
     * @param name Warehouse name
     * @param address Warehouse physical address
     * @return A new warehouse request with inactive status
     */
    public static SharedWarehouseRequestDTO createInactive(String code, String name, SharedAddressValue address) {
        return SharedWarehouseRequestDTO.builder()
            .code(code)
            .name(name)
            .address(address)
            .isActive(false)
            .build();
    }
}