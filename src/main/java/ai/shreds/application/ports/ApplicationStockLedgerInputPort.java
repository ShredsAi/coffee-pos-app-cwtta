package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedStockLedgerResponseDTO;
import ai.shreds.shared.dtos.SharedStockLedgerQueryParams;

import java.util.UUID;

/**
 * Input port for stock ledger query operations.
 * This interface defines the contract for retrieving stock movement history
 * and current inventory quantities in the application layer.
 */
public interface ApplicationStockLedgerInputPort {

    /**
     * Retrieves the stock ledger for a specific product in a specific warehouse.
     * The stock ledger includes current inventory quantities and a paginated
     * history of stock movements with filtering options.
     *
     * @param warehouseId The ID of the warehouse to query
     * @param productId The ID of the product to query
     * @param queryParams The query parameters including pagination, date range, and movement type filtering
     * @return SharedStockLedgerResponseDTO The stock ledger containing current quantities and movement history
     * @throws ai.shreds.application.exceptions.ApplicationEntityNotFoundException if warehouse or product not found
     */
    SharedStockLedgerResponseDTO getStockLedger(UUID warehouseId, UUID productId, SharedStockLedgerQueryParams queryParams);
}