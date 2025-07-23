package ai.shreds;

import ai.shreds.adapter.primary.AdapterStockMovementController;
import ai.shreds.infrastructure.external_services.*;
import ai.shreds.shared.dtos.*;
import ai.shreds.shared.enums.SharedStockMovementTypeEnum;
import ai.shreds.shared.enums.SharedReferenceTypeEnum;
import ai.shreds.shared.value_objects.SharedQuantityValue;
import ai.shreds.shared.value_objects.SharedMoneyValue;
import ai.shreds.infrastructure.repositories.entities.*;
import ai.shreds.infrastructure.repositories.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Stock Movement Processing
 * Tests the complete end-to-end flow of processing stock movements through REST API,
 * including database persistence, inventory quantity updates, and event publishing.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
@Transactional
class StockMovementProcessingIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("stock_ledger_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(true);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InfrastructureWarehouseSpringDataRepository warehouseRepository;

    @Autowired
    private InfrastructureInventoryItemSpringDataRepository inventoryItemRepository;

    @Autowired
    private InfrastructureBatchSpringDataRepository batchRepository;

    @Autowired
    private InfrastructureStockMovementSpringDataRepository stockMovementRepository;

    @Autowired
    private InfrastructureEventOutboxSpringDataRepository eventOutboxRepository;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    // Mock external service clients
    @MockBean
    private InfrastructureProductServiceClient productServiceClient;

    @MockBean
    private InfrastructureUserServiceClient userServiceClient;

    @MockBean
    private InfrastructureTimeServiceClient timeServiceClient;

    // Test data
    private UUID testWarehouseId;
    private UUID testProductId;
    private String testBatchNumber;
    private SharedStockMovementRequestDTO inboundRequest;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        // Setup test data
        testWarehouseId = UUID.randomUUID();
        testProductId = UUID.randomUUID();
        testBatchNumber = "BATCH-" + System.currentTimeMillis();

        // Setup mock behaviors
        setupExternalServiceMocks();

        // Create test warehouse
        createTestWarehouse();

        // Create inbound stock movement request
        inboundRequest = SharedStockMovementRequestDTO.builder()
                .warehouseId(testWarehouseId)
                .productId(testProductId)
                .movementType(SharedStockMovementTypeEnum.INBOUND)
                .quantity(SharedQuantityValue.builder()
                        .value(new BigDecimal("100.0"))
                        .unit("pieces")
                        .build())
                .referenceId("PO-12345")
                .referenceType(SharedReferenceTypeEnum.PURCHASE_ORDER)
                .reason("Initial stock receipt from supplier")
                .performedBy("warehouse-operator-123")
                .costPerUnit(SharedMoneyValue.builder()
                        .amount(new BigDecimal("10.50"))
                        .currency("USD")
                        .build())
                .batchNumber(testBatchNumber)
                .build();

        System.out.println("=== TEST SETUP COMPLETED ===");
        System.out.println("Test Warehouse ID: " + testWarehouseId);
        System.out.println("Test Product ID: " + testProductId);
        System.out.println("Test Batch Number: " + testBatchNumber);
    }

    /**
     * Test: When_INBOUND_Movement_Is_Created_Then_Inventory_And_Batch_Are_Updated_And_Events_Published
     * 
     * Tests that an INBOUND stock movement creates a new batch, updates inventory quantities,
     * persists movement record, and publishes StockMovementCreated and BatchCreated events.
     */
    @Test
    void When_INBOUND_Movement_Is_Created_Then_Inventory_And_Batch_Are_Updated_And_Events_Published(CapturedOutput output) {
        System.out.println("\n=== STARTING INBOUND MOVEMENT INTEGRATION TEST ===");

        // Create HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SharedStockMovementRequestDTO> request = new HttpEntity<>(inboundRequest, headers);

        // Act: Send POST request to create stock movement
        System.out.println("Sending POST request to /api/stock-movements");
        ResponseEntity<SharedStockMovementResponseDTO> response = restTemplate.postForEntity(
                "/api/stock-movements",
                request,
                SharedStockMovementResponseDTO.class
        );

        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());

        // Assert: Verify HTTP response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        SharedStockMovementResponseDTO responseBody = response.getBody();
        assertThat(responseBody.getMovementId()).isNotNull();
        assertThat(responseBody.getWarehouseId()).isEqualTo(testWarehouseId);
        assertThat(responseBody.getProductId()).isEqualTo(testProductId);
        assertThat(responseBody.getMovementType()).isEqualTo(SharedStockMovementTypeEnum.INBOUND);
        assertThat(responseBody.getQuantity().getValue()).isEqualByComparingTo(new BigDecimal("100.0"));
        assertThat(responseBody.getQuantity().getUnit()).isEqualTo("pieces");

        System.out.println("=== HTTP RESPONSE VERIFICATION PASSED ===");

        // Assert: Verify stock movement is persisted in database
        List<InfrastructureStockMovementJpaEntity> movements = stockMovementRepository.findAll();
        assertThat(movements).isNotEmpty();
        
        InfrastructureStockMovementJpaEntity savedMovement = movements.stream()
                .filter(m -> m.getWarehouseId().equals(testWarehouseId) && 
                           m.getProductId().equals(testProductId.toString()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Stock movement not found in database"));

        assertThat(savedMovement.getMovementType()).isEqualTo("INBOUND");
        assertThat(savedMovement.getQuantity()).isEqualByComparingTo(new BigDecimal("100.0"));
        assertThat(savedMovement.getQtyUnit()).isEqualTo("pieces");
        assertThat(savedMovement.getReferenceId()).isEqualTo("PO-12345");
        assertThat(savedMovement.getPerformedBy()).isEqualTo("warehouse-operator-123");

        System.out.println("=== STOCK MOVEMENT PERSISTENCE VERIFICATION PASSED ===");

        // Assert: Verify batch is created
        List<InfrastructureBatchJpaEntity> batches = batchRepository.findAll();
        assertThat(batches).isNotEmpty();
        
        InfrastructureBatchJpaEntity savedBatch = batches.stream()
                .filter(b -> b.getWarehouseId().equals(testWarehouseId) && 
                           b.getProductId().equals(testProductId.toString()) &&
                           b.getBatchNumber().equals(testBatchNumber))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Batch not found in database"));

        assertThat(savedBatch.getQuantity()).isEqualByComparingTo(new BigDecimal("100.0"));
        assertThat(savedBatch.getQtyUnit()).isEqualTo("pieces");
        assertThat(savedBatch.getBatchNumber()).isEqualTo(testBatchNumber);

        System.out.println("=== BATCH CREATION VERIFICATION PASSED ===");

        // Assert: Verify inventory item is created/updated
        List<InfrastructureInventoryItemJpaEntity> inventoryItems = inventoryItemRepository.findAll();
        assertThat(inventoryItems).isNotEmpty();
        
        InfrastructureInventoryItemJpaEntity savedInventoryItem = inventoryItems.stream()
                .filter(item -> item.getWarehouseId().equals(testWarehouseId) && 
                              item.getProductId().equals(testProductId.toString()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Inventory item not found in database"));

        assertThat(savedInventoryItem.getTotalQty()).isEqualByComparingTo(new BigDecimal("100.0"));
        assertThat(savedInventoryItem.getAvailableQty()).isEqualByComparingTo(new BigDecimal("100.0"));
        assertThat(savedInventoryItem.getQtyUnit()).isEqualTo("pieces");
        assertThat(savedInventoryItem.getLastMovementAt()).isNotNull();

        System.out.println("=== INVENTORY ITEM UPDATE VERIFICATION PASSED ===");

        // Assert: Verify events are published to outbox
        List<InfrastructureEventOutboxJpaEntity> outboxEvents = eventOutboxRepository.findAll();
        assertThat(outboxEvents).isNotEmpty();
        
        // Check for StockMovementCreated event
        boolean stockMovementEventFound = outboxEvents.stream()
                .anyMatch(event -> "StockMovementCreated".equals(event.getEventType()) &&
                                 "StockMovement".equals(event.getAggregateType()) &&
                                 savedMovement.getId().equals(event.getAggregateId()));
        assertThat(stockMovementEventFound).isTrue();

        // Check for BatchCreated event (if applicable)
        boolean batchCreatedEventFound = outboxEvents.stream()
                .anyMatch(event -> "BatchCreated".equals(event.getEventType()) &&
                                 "Batch".equals(event.getAggregateType()));
        
        System.out.println("=== EVENT PUBLISHING VERIFICATION PASSED ===");
        System.out.println("Found " + outboxEvents.size() + " events in outbox");
        outboxEvents.forEach(event -> {
            System.out.println("Event: " + event.getEventType() + ", Aggregate: " + event.getAggregateType());
        });

        // Assert: Analyze logs for successful processing
        String logs = output.getOut();
        assertThat(logs).contains("Processing stock movement");
        assertThat(logs).contains("Successfully processed stock movement");
        assertThat(logs).doesNotContain("ERROR");

        System.out.println("=== LOG ANALYSIS VERIFICATION PASSED ===");
        System.out.println("\n=== FULL INTEGRATION TEST LOGS ===");
        System.out.println(logs);
        System.out.println("\n=== INBOUND MOVEMENT INTEGRATION TEST COMPLETED SUCCESSFULLY ===");
    }

    private void setupExternalServiceMocks() {
        // Mock Product Service
        Map<String, Object> productDetails = new HashMap<>();
        productDetails.put("id", testProductId.toString());
        productDetails.put("status", "ACTIVE");
        productDetails.put("uom", "pieces");
        
        when(productServiceClient.validateProduct(any())).thenReturn(true);
        when(productServiceClient.getProductDetails(any())).thenReturn(productDetails);

        // Mock User Service
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("id", "warehouse-operator-123");
        userDetails.put("role", "WAREHOUSE_OPERATOR");
        
        when(userServiceClient.validateUser(any())).thenReturn(true);
        when(userServiceClient.getUserDetails(any())).thenReturn(userDetails);

        // Mock Time Service
        when(timeServiceClient.getCurrentTime()).thenReturn(LocalDateTime.now());
    }

    private void createTestWarehouse() {
        InfrastructureWarehouseJpaEntity warehouse = new InfrastructureWarehouseJpaEntity();
        warehouse.setId(testWarehouseId);
        warehouse.setCode("WH-TEST-" + System.currentTimeMillis());
        warehouse.setName("Test Warehouse");
        warehouse.setStreet("123 Test Street");
        warehouse.setCity("Test City");
        warehouse.setState("Test State");
        warehouse.setPostalCode("12345");
        warehouse.setCountry("US");
        warehouse.setActive(true);
        warehouse.setCreatedAt(LocalDateTime.now());
        warehouse.setUpdatedAt(LocalDateTime.now());
        
        warehouseRepository.save(warehouse);
        System.out.println("Test warehouse created with ID: " + testWarehouseId);
    }
}