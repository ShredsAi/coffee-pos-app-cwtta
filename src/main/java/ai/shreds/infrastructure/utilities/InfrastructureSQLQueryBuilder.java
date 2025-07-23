package ai.shreds.infrastructure.utilities;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.StringJoiner;

/**
 * Utility class for building dynamic SQL queries for stock movement filtering.
 * Provides methods to construct complex WHERE clauses and pagination.
 */
@Component
public class InfrastructureSQLQueryBuilder {

    /**
     * Builds a stock movement query with filters.
     * @param filters map of filter criteria
     * @return SQL query string
     */
    public String buildStockMovementQuery(Map<String, Object> filters) {
        StringBuilder query = new StringBuilder("SELECT * FROM stock_movement WHERE 1=1");
        
        if (filters.containsKey("warehouseId") && filters.get("warehouseId") != null) {
            query.append(" AND warehouse_id = :warehouseId");
        }
        
        if (filters.containsKey("productId") && filters.get("productId") != null) {
            query.append(" AND product_id = :productId");
        }
        
        if (filters.containsKey("movementType") && filters.get("movementType") != null) {
            query.append(" AND movement_type = :movementType");
        }
        
        if (filters.containsKey("startDate") && filters.get("startDate") != null) {
            query.append(" AND performed_at >= :startDate");
        }
        
        if (filters.containsKey("endDate") && filters.get("endDate") != null) {
            query.append(" AND performed_at <= :endDate");
        }
        
        if (filters.containsKey("referenceType") && filters.get("referenceType") != null) {
            query.append(" AND reference_type = :referenceType");
        }
        
        if (filters.containsKey("performedBy") && filters.get("performedBy") != null) {
            query.append(" AND performed_by = :performedBy");
        }
        
        return query.toString();
    }

    /**
     * Builds a pagination clause for SQL queries.
     * @param page the page number (0-based)
     * @param size the page size
     * @return pagination SQL clause
     */
    public String buildPaginationClause(Integer page, Integer size) {
        if (page == null || size == null || page < 0 || size <= 0) {
            return "";
        }
        
        int offset = page * size;
        return String.format(" LIMIT %d OFFSET %d", size, offset);
    }

    /**
     * Builds an ORDER BY clause with field and direction.
     * @param sortField the field to sort by
     * @param sortDirection the sort direction (ASC/DESC)
     * @return ORDER BY SQL clause
     */
    public String buildOrderByClause(String sortField, String sortDirection) {
        if (sortField == null || sortField.trim().isEmpty()) {
            return " ORDER BY performed_at DESC";
        }
        
        // Whitelist allowed sort fields for security
        String allowedField = validateSortField(sortField);
        String direction = "DESC".equalsIgnoreCase(sortDirection) ? "DESC" : "ASC";
        
        return String.format(" ORDER BY %s %s", allowedField, direction);
    }

    /**
     * Validates and maps sort field names to prevent SQL injection.
     * @param sortField the requested sort field
     * @return validated field name
     */
    private String validateSortField(String sortField) {
        switch (sortField.toLowerCase()) {
            case "performedat":
            case "performed_at":
                return "performed_at";
            case "createdat":
            case "created_at":
                return "created_at";
            case "movementtype":
            case "movement_type":
                return "movement_type";
            case "quantity":
                return "quantity";
            case "performedby":
            case "performed_by":
                return "performed_by";
            default:
                return "performed_at"; // default fallback
        }
    }

    /**
     * Builds a count query for pagination metadata.
     * @param filters map of filter criteria
     * @return count SQL query string
     */
    public String buildCountQuery(Map<String, Object> filters) {
        String baseQuery = buildStockMovementQuery(filters);
        return baseQuery.replaceFirst("SELECT \\* FROM", "SELECT COUNT(*) FROM");
    }
}