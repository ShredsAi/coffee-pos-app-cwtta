package ai.shreds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Main Spring Boot Application class for Stock Ledger Management System.
 * 
 * This application provides comprehensive inventory management functionality including:
 * - Stock movement tracking (INBOUND, OUTBOUND, TRANSFER, ADJUSTMENT)
 * - Warehouse management
 * - Batch tracking with FIFO allocation
 * - Inventory item quantity management
 * - Event-driven architecture with transactional outbox pattern
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@EnableAspectJAutoProxy
public class StockLedgerManagementApplication {

    /**
     * Main method to bootstrap the Spring Boot application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(StockLedgerManagementApplication.class, args);
    }
}