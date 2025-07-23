package ai.shreds.domain.commands;

import ai.shreds.domain.value_objects.DomainAddressValue;

/**
 * Command object for creating a new warehouse.
 * Contains all the data needed to create a warehouse entity.
 */
public class DomainCreateWarehouseCommand {
    private final String code;
    private final String name;
    private final DomainAddressValue address;
    private final boolean isActive;

    /**
     * Constructs a new warehouse creation command.
     *
     * @param code the warehouse code (business identifier)
     * @param name the warehouse name
     * @param address the warehouse address
     * @param isActive whether the warehouse should be initially active
     */
    public DomainCreateWarehouseCommand(
            String code,
            String name,
            DomainAddressValue address,
            boolean isActive) {
        this.code = code;
        this.name = name;
        this.address = address;
        this.isActive = isActive;
    }

    /**
     * Gets the warehouse code.
     *
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the warehouse name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the warehouse address.
     *
     * @return the address
     */
    public DomainAddressValue getAddress() {
        return address;
    }

    /**
     * Gets the initial active status.
     *
     * @return true if warehouse should be active
     */
    public boolean isActive() {
        return isActive;
    }

    @Override
    public String toString() {
        return "DomainCreateWarehouseCommand{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", address=" + address +
                ", isActive=" + isActive +
                '}';
    }
}