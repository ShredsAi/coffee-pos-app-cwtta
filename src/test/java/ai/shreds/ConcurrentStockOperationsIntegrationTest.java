package ai.shreds;

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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for concurrent stock operations to ensure optimistic locking prevents overselling.
 * Tests concurrent OUTBOUND movements on the same inventory item to verify data consistency.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
class ConcurrentStockOperationsIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("stock_ledger_concurrent_test")
            .withUsername("concurrent_test_user")
            .withPassword("concurrent_test_password")
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
    private UUID initialInventoryItemId;
    private final BigDecimal INITIAL_AVAILABLE_STOCK = new BigDecimal("100.0");
    private final BigDecimal CONCURRENT_REQUEST_QUANTITY = new BigDecimal("30.0");
    private final int NUMBER_OF_CONCURRENT_REQUESTS = 4; // 4 * 30 = 120 > 100 available stock

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Enable more aggressive transaction timeout for testing
        registry.add("spring.transaction.default-timeout", "10");
    }

    @BeforeEach
    void setUp() {
        // Setup test data
        testWarehouseId = UUID.randomUUID();
        testProductId = UUID.randomUUID();
        initialInventoryItemId = UUID.randomUUID();

        // Setup mock behaviors
        setupExternalServiceMocks();

        // Create test warehouse
        createTestWarehouse();

        // Create initial inventory with stock
        createInitialInventoryWithStock();

        // Create initial batches for FIFO allocation
        createInitialBatches();

        System.out.println("=== CONCURRENT TEST SETUP COMPLETED ===");
        System.out.println("Test Warehouse ID: " + testWarehouseId);
        System.out.println("Test Product ID: " + testProductId);
        System.out.println("Initial Available Stock: " + INITIAL_AVAILABLE_STOCK);
        System.out.println("Concurrent Request Quantity (each): " + CONCURRENT_REQUEST_QUANTITY);
        System.out.println("Number of Concurrent Requests: " + NUMBER_OF_CONCURRENT_REQUESTS);
        System.out.println("Total Requested Quantity: " + CONCURRENT_REQUEST_QUANTITY.multiply(BigDecimal.valueOf(NUMBER_OF_CONCURRENT_REQUESTS)));
    }

    /**
     * Test: When_Concurrent_OUTBOUND_Movements_Occur_Then_Optimistic_Locking_Prevents_Overselling
     * 
     * Tests that multiple simultaneous OUTBOUND movements on the same inventory item use optimistic 
     * locking to prevent available quantity from going negative. The test creates 4 concurrent requests
     * each trying to consume 30 units from an initial stock of 100 units. Since 4 * 30 = 120 > 100,
     * at least one request should fail due to optimistic locking, preventing overselling.
     */
    @Test
    void When_Concurrent_OUTBOUND_Movements_Occur_Then_Optimistic_Locking_Prevents_Overselling(CapturedOutput output) {
        System.out.println("\n=== STARTING CONCURRENT OUTBOUND MOVEMENTS TEST ===");

        // Verify initial state
        Optional<InfrastructureInventoryItemJpaEntity> initialInventory = 
            inventoryItemRepository.findById(initialInventoryItemId);
        assertThat(initialInventory).isPresent();
        assertThat(initialInventory.get().getAvailableQty()).isEqualByComparingTo(INITIAL_AVAILABLE_STOCK);
        assertThat(initialInventory.get().getTotalQty()).isEqualByComparingTo(INITIAL_AVAILABLE_STOCK);
        System.out.println("Initial inventory verified: Available=" + initialInventory.get().getAvailableQty() + 
                          ", Total=" + initialInventory.get().getTotalQty() + 
                          ", Version=" + initialInventory.get().getVersion());

        // Create concurrent outbound requests
        List<SharedStockMovementRequestDTO> concurrentRequests = createConcurrentOutboundRequests();

        // Execute concurrent requests
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_CONCURRENT_REQUESTS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(NUMBER_OF_CONCURRENT_REQUESTS);
        
        List<Future<ResponseEntity<SharedStockMovementResponseDTO>>> futures = new ArrayList<>();
        AtomicInteger successfulRequests = new AtomicInteger(0);
        AtomicInteger failedRequests = new AtomicInteger(0);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        System.out.println("\nStarting " + NUMBER_OF_CONCURRENT_REQUESTS + " concurrent OUTBOUND requests...");
        
        // Submit all requests
        for (int i = 0; i < NUMBER_OF_CONCURRENT_REQUESTS; i++) {
            final int requestIndex = i;
            final SharedStockMovementRequestDTO request = concurrentRequests.get(i);
            
            Future<ResponseEntity<SharedStockMovementResponseDTO>> future = executorService.submit(() -> {
                try {
                    // Wait for all threads to start simultaneously
                    startLatch.await();
                    
                    System.out.println("Thread " + requestIndex + " executing OUTBOUND request...");
                    
                    // Execute the HTTP request
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<SharedStockMovementRequestDTO> httpEntity = new HttpEntity<>(request, headers);

                    ResponseEntity<SharedStockMovementResponseDTO> response = restTemplate.postForEntity(
                            "/api/stock-movements",
                            httpEntity,
                            SharedStockMovementResponseDTO.class
                    );

                    System.out.println("Thread " + requestIndex + " completed with status: " + response.getStatusCode());
                    
                    if (response.getStatusCode().is2xxSuccessful()) {
                        successfulRequests.incrementAndGet();
                    } else {
                        failedRequests.incrementAndGet();
                    }
                    
                    return response;
                    
                } catch (Exception e) {
                    System.err.println("Thread " + requestIndex + " failed with exception: " + e.getMessage());
                    exceptions.add(e);
                    failedRequests.incrementAndGet();
                    throw new RuntimeException(e);
                } finally {
                    doneLatch.countDown();
                }
            });
            
            futures.add(future);
        }

        // Start all threads simultaneously
        System.out.println("\nReleasing all threads to execute concurrently...");
        startLatch.countDown();

        // Wait for all requests to complete (with timeout)
        try {
            boolean allCompleted = doneLatch.await(30, TimeUnit.SECONDS);
            assertThat(allCompleted).isTrue();
            System.out.println("All concurrent requests completed.");
        } catch (InterruptedException e) {
            fail("Concurrent execution was interrupted");
        } finally {
            executorService.shutdown();
        }

        // Collect results
        List<ResponseEntity<SharedStockMovementResponseDTO>> responses = new ArrayList<>();
        List<Exception> executionExceptions = new ArrayList<>();
        
        for (Future<ResponseEntity<SharedStockMovementResponseDTO>> future : futures) {
            try {
                responses.add(future.get());
            } catch (ExecutionException e) {
                executionExceptions.add(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Thread was interrupted");
            }
        }
        
        System.out.println("\n=== CONCURRENT EXECUTION RESULTS ===");
        System.out.println("Successful HTTP responses: " + responses.size());
        System.out.println("Execution exceptions: " + executionExceptions.size());
        System.out.println("Total successful requests: " + successfulRequests.get());
        System.out.println("Total failed requests: " + failedRequests.get());

        // Analyze responses
        long successfulResponses = responses.stream()
                .map(ResponseEntity::getStatusCode)
                .filter(HttpStatus::is2xxSuccessful)
                .count();
                
        long conflictResponses = responses.stream()
                .map(ResponseEntity::getStatusCode)
                .filter(status -> status == HttpStatus.CONFLICT)
                .count();

        System.out.println("Successful (2XX) responses: " + successfulResponses);
        System.out.println("Conflict (409) responses: " + conflictResponses);

        // ASSERT: Verify that optimistic locking prevented overselling
        // The key assertion: not all requests should succeed due to optimistic locking
        // Since we're requesting 4 * 30 = 120 units from 100 available, at least one should fail
        int totalProcessedRequests = responses.size();
        assertThat(totalProcessedRequests).isGreaterThan(0);
        
        // At least some requests should have failed due to optimistic locking or insufficient stock
        // We expect that successful requests * 30 <= 100 (initial stock)
        BigDecimal totalSuccessfullyProcessedQuantity = BigDecimal.valueOf(successfulResponses)
                .multiply(CONCURRENT_REQUEST_QUANTITY);
        assertThat(totalSuccessfullyProcessedQuantity).isLessThanOrEqualTo(INITIAL_AVAILABLE_STOCK);
        
        System.out.println("\n=== OPTIMISTIC LOCKING VERIFICATION ===");
        System.out.println("Expected: Not all requests succeed due to optimistic locking");
        System.out.println("Actual: " + successfulResponses + " successful out of " + NUMBER_OF_CONCURRENT_REQUESTS + " requests");
        System.out.println("Total successfully processed quantity: " + totalSuccessfullyProcessedQuantity);
        System.out.println("Initial available stock: " + INITIAL_AVAILABLE_STOCK);
        
        // Verify final inventory state
        Optional<InfrastructureInventoryItemJpaEntity> finalInventory = 
            inventoryItemRepository.findById(initialInventoryItemId);
        assertThat(finalInventory).isPresent();
        
        BigDecimal finalAvailableQty = finalInventory.get().getAvailableQty();
        BigDecimal finalTotalQty = finalInventory.get().getTotalQty();
        Long finalVersion = finalInventory.get().getVersion();
        
        System.out.println("\n=== FINAL INVENTORY STATE VERIFICATION ===");
        System.out.println("Final Available Quantity: " + finalAvailableQty);
        System.out.println("Final Total Quantity: " + finalTotalQty);
        System.out.println("Final Version: " + finalVersion);
        System.out.println("Initial Version: " + initialInventory.get().getVersion());
        
        // ASSERT: Final inventory must not go negative (key business rule)
        assertThat(finalAvailableQty).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(finalTotalQty).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        
        // ASSERT: Final quantity should be initial - successful quantity
        BigDecimal expectedFinalQuantity = INITIAL_AVAILABLE_STOCK.subtract(totalSuccessfullyProcessedQuantity);
        assertThat(finalAvailableQty).isEqualByComparingTo(expectedFinalQuantity);
        assertThat(finalTotalQty).isEqualByComparingTo(expectedFinalQuantity);
        
        // ASSERT: Version should have been incremented (proving optimistic locking was used)
        assertThat(finalVersion).isGreaterThan(initialInventory.get().getVersion());
        
        // ASSERT: Verify stock movements were persisted correctly
        List<InfrastructureStockMovementJpaEntity> stockMovements = stockMovementRepository.findAll();
        List<InfrastructureStockMovementJpaEntity> outboundMovements = stockMovements.stream()
                .filter(movement -> "OUTBOUND".equals(movement.getMovementType()) &&
                                  movement.getWarehouseId().equals(testWarehouseId) &&
                                  movement.getProductId().equals(testProductId.toString()))
                .toList();
        
        System.out.println("\n=== STOCK MOVEMENTS VERIFICATION ===");
        System.out.println("Total stock movements in DB: " + stockMovements.size());
        System.out.println("OUTBOUND movements for test product: " + outboundMovements.size());
        
        // The number of persisted OUTBOUND movements should equal successful responses
        assertThat(outboundMovements.size()).isEqualTo((int) successfulResponses);
        
        // Verify each persisted movement has correct data
        for (InfrastructureStockMovementJpaEntity movement : outboundMovements) {
            assertThat(movement.getMovementType()).isEqualTo("OUTBOUND");
            assertThat(movement.getQuantity()).isEqualByComparingTo(CONCURRENT_REQUEST_QUANTITY);
            assertThat(movement.getQtyUnit()).isEqualTo("pieces");
            assertThat(movement.getWarehouseId()).isEqualTo(testWarehouseId);
            assertThat(movement.getProductId()).isEqualTo(testProductId.toString());
            assertThat(movement.getPerformedBy()).startsWith("concurrent-operator-");
        }
        
        // ASSERT: Verify events were published correctly
        List<InfrastructureEventOutboxJpaEntity> outboxEvents = eventOutboxRepository.findAll();
        long stockMovementEvents = outboxEvents.stream()
                .filter(event -> "StockMovementCreated".equals(event.getEventType()))
                .count();
        long stockLevelsUpdatedEvents = outboxEvents.stream()
                .filter(event -> "StockLevelsUpdated".equals(event.getEventType()))
                .count();
                
        System.out.println("\n=== EVENT PUBLISHING VERIFICATION ===");
        System.out.println("Total outbox events: " + outboxEvents.size());
        System.out.println("StockMovementCreated events: " + stockMovementEvents);
        System.out.println("StockLevelsUpdated events: " + stockLevelsUpdatedEvents);
        
        // Should have StockMovementCreated events equal to successful movements
        assertThat(stockMovementEvents).isEqualTo(successfulResponses);
        // Should have StockLevelsUpdated events (could be consolidated or individual)
        assertThat(stockLevelsUpdatedEvents).isGreaterThan(0);
        
        // ASSERT: Analyze logs for expected behavior
        String logs = output.getOut();
        assertThat(logs).contains("OUTBOUND");
        
        // Should contain successful processing logs
        if (successfulResponses > 0) {
            assertThat(logs).contains("Successfully processed stock movement");
        }
        
        // Should not contain any ERROR logs (failures should be handled gracefully)
        assertThat(logs).doesNotContain("ERROR");
        
        // Log exceptions if any occurred
        if (!exceptions.isEmpty()) {
            System.out.println("\n=== EXCEPTIONS DURING CONCURRENT EXECUTION ===");
            exceptions.forEach(e -> {
                System.err.println("Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            });
        }
        
        System.out.println("\n=== CONCURRENT OPERATIONS TEST SUMMARY ===");
        System.out.println("✓ Optimistic locking successfully prevented overselling");
        System.out.println("✓ Final inventory quantities are non-negative and consistent");
        System.out.println("✓ Only " + successfulResponses + " out of " + NUMBER_OF_CONCURRENT_REQUESTS + " requests succeeded");
        System.out.println("✓ Total processed quantity (" + totalSuccessfullyProcessedQuantity + 
                          ") did not exceed initial stock (" + INITIAL_AVAILABLE_STOCK + ")");
        System.out.println("✓ Stock movements and events were persisted correctly");
        System.out.println("✓ Entity version was properly incremented indicating optimistic locking usage");
        
        System.out.println("\n=== FULL CONCURRENT TEST LOGS ===");
        System.out.println(logs);
        System.out.println("\n=== CONCURRENT STOCK OPERATIONS TEST COMPLETED SUCCESSFULLY ===");
    }

    private List<SharedStockMovementRequestDTO> createConcurrentOutboundRequests() {
        List<SharedStockMovementRequestDTO> requests = new ArrayList<>();
        
        for (int i = 0; i < NUMBER_OF_CONCURRENT_REQUESTS; i++) {
            SharedStockMovementRequestDTO request = SharedStockMovementRequestDTO.builder()
                    .warehouseId(testWarehouseId)
                    .productId(testProductId)
                    .movementType(SharedStockMovementTypeEnum.OUTBOUND)
                    .quantity(SharedQuantityValue.builder()
                            .value(CONCURRENT_REQUEST_QUANTITY)
                            .unit("pieces")
                            .build())
                    .referenceId("CONCURRENT-SO-" + i + "-" + System.currentTimeMillis())
                    .referenceType(SharedReferenceTypeEnum.SALES_ORDER)
                    .reason("Concurrent order fulfillment test - request #" + i)
                    .performedBy("concurrent-operator-" + i)
                    .costPerUnit(SharedMoneyValue.builder()
                            .amount(new BigDecimal("15.00"))
                            .currency("USD")
                            .build())
                    .build();
            
            requests.add(request);
        }
        
        return requests;
    }

    private void setupExternalServiceMocks() {
        // Mock Product Service
        Map<String, Object> productDetails = new HashMap<>();
        productDetails.put("id", testProductId.toString());
        productDetails.put("status", "ACTIVE");
        productDetails.put("uom", "pieces");
        
        when(productServiceClient.validateProduct(any())).thenReturn(true);
        when(productServiceClient.getProductDetails(any())).thenReturn(productDetails);

        // Mock User Service for multiple concurrent operators
        when(userServiceClient.validateUser(any())).thenReturn(true);
        when(userServiceClient.getUserDetails(any())).thenAnswer(invocation -> {
            String userId = (String) invocation.getArgument(0);
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("id", userId);
            userDetails.put("role", "WAREHOUSE_OPERATOR");
            return userDetails;
        });

        // Mock Time Service
        when(timeServiceClient.getCurrentTime()).thenReturn(LocalDateTime.now());
    }

    private void createTestWarehouse() {
        InfrastructureWarehouseJpaEntity warehouse = new InfrastructureWarehouseJpaEntity();
        warehouse.setId(testWarehouseId);
        warehouse.setCode("WH-CONCURRENT-" + System.currentTimeMillis());
        warehouse.setName("Concurrent Test Warehouse");
        warehouse.setStreet("123 Concurrent Test Street");
        warehouse.setCity("Test City");
        warehouse.setState("Test State");
        warehouse.setPostalCode("12345");
        warehouse.setCountry("US");
        warehouse.setActive(true);
        warehouse.setCreatedAt(LocalDateTime.now());
        warehouse.setUpdatedAt(LocalDateTime.now());
        
        warehouseRepository.save(warehouse);
        System.out.println("Concurrent test warehouse created with ID: " + testWarehouseId);
    }

    private void createInitialInventoryWithStock() {
        InfrastructureInventoryItemJpaEntity inventoryItem = new InfrastructureInventoryItemJpaEntity();
        inventoryItem.setId(initialInventoryItemId);
        inventoryItem.setWarehouseId(testWarehouseId);
        inventoryItem.setProductId(testProductId.toString());
        inventoryItem.setTotalQty(INITIAL_AVAILABLE_STOCK);
        inventoryItem.setAvailableQty(INITIAL_AVAILABLE_STOCK);
        inventoryItem.setReservedQty(BigDecimal.ZERO);
        inventoryItem.setAllocatedQty(BigDecimal.ZERO);
        inventoryItem.setQtyUnit("pieces");
        inventoryItem.setSafetyStockLevel(new BigDecimal("10.0"));
        inventoryItem.setReorderPoint(new BigDecimal("20.0"));
        inventoryItem.setLastMovementAt(LocalDateTime.now().minusHours(1));
        inventoryItem.setVersion(1L); // Initial version for optimistic locking
        
        inventoryItemRepository.save(inventoryItem);
        System.out.println("Initial inventory item created with ID: " + initialInventoryItemId + 
                          ", Available Stock: " + INITIAL_AVAILABLE_STOCK);
    }

    private void createInitialBatches() {
        // Create a batch with sufficient stock for the test
        InfrastructureBatchJpaEntity batch = new InfrastructureBatchJpaEntity();
        batch.setId(UUID.randomUUID());
        batch.setWarehouseId(testWarehouseId);
        batch.setProductId(testProductId.toString());
        batch.setBatchNumber("CONCURRENT-BATCH-" + System.currentTimeMillis());
        batch.setQuantity(INITIAL_AVAILABLE_STOCK);
        batch.setQtyUnit("pieces");
        batch.setManufacturingDate(LocalDate.now().minusDays(5));
        batch.setExpirationDate(LocalDate.now().plusDays(100));
        batch.setReceivedAt(LocalDateTime.now().minusHours(1));
        batch.setSupplierId(UUID.randomUUID());
        batch.setVersion(1L);
        
        batchRepository.save(batch);
        System.out.println("Initial batch created with quantity: " + INITIAL_AVAILABLE_STOCK);
    }
}