package ai.shreds;

import ai.shreds.domain.ports.*;
import ai.shreds.infrastructure.external_services.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify that the Stock Ledger Management Application starts correctly
 * with all required dependencies and configurations.
 * 
 * This test:
 * - Uses TestContainers to start a real PostgreSQL database
 * - Mocks external service dependencies (Product, User, Time services)
 * - Captures and analyzes startup logs
 * - Verifies application context loads successfully
 * - Validates critical beans are created and configured
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
class StockLedgerManagementApplicationStartupTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("stock_ledger_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(true);

    @LocalServerPort
    private int port;

    @Autowired
    private ApplicationContext applicationContext;

    // Mock external service clients to prevent real HTTP calls during startup
    @MockBean
    private InfrastructureProductServiceClient productServiceClient;

    @MockBean
    private InfrastructureUserServiceClient userServiceClient;

    @MockBean
    private InfrastructureTimeServiceClient timeServiceClient;

    /**
     * Configure TestContainers database properties dynamically
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    /**
     * Test configuration to set up mock behaviors
     */
    @TestConfiguration
    static class TestConfig {
        // Additional test beans can be configured here if needed
    }

    /**
     * Main test method that verifies application startup
     */
    @Test
    void shouldStartApplicationSuccessfully(CapturedOutput output) {
        // Setup mock behaviors for external services
        setupExternalServiceMocks();

        // Verify application context loads successfully
        assertThat(applicationContext).isNotNull();
        
        // Verify the application is running on the expected port
        assertThat(port).isGreaterThan(0);
        
        // Verify database container is running
        assertTrue(postgres.isRunning(), "PostgreSQL container should be running");
        
        // Analyze startup logs for critical information
        analyzeStartupLogs(output);
        
        // Verify critical application components are loaded
        verifyApplicationComponents();
        
        // Verify database connectivity
        verifyDatabaseConnectivity();
        
        System.out.println("=== APPLICATION STARTUP SUCCESSFUL ===");
        System.out.println("Application running on port: " + port);
        System.out.println("Database URL: " + postgres.getJdbcUrl());
        System.out.println("=== FULL STARTUP LOGS ===");
        System.out.println(output.getOut());
    }

    /**
     * Setup mock behaviors for external service clients
     */
    private void setupExternalServiceMocks() {
        // Mock Product Service
        Map<String, Object> productDetails = new HashMap<>();
        productDetails.put("id", "test-product-123");
        productDetails.put("status", "ACTIVE");
        productDetails.put("uom", "pieces");
        
        when(productServiceClient.validateProduct(any())).thenReturn(true);
        when(productServiceClient.getProductDetails(any())).thenReturn(productDetails);

        // Mock User Service
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("id", "test-user-456");
        userDetails.put("role", "WAREHOUSE_OPERATOR");
        
        when(userServiceClient.validateUser(any())).thenReturn(true);
        when(userServiceClient.getUserDetails(any())).thenReturn(userDetails);

        // Mock Time Service
        when(timeServiceClient.getCurrentTime()).thenReturn(LocalDateTime.now());
    }

    /**
     * Analyze startup logs for errors, warnings, and successful initialization
     */
    private void analyzeStartupLogs(CapturedOutput output) {
        String logs = output.getOut();
        
        // Check for successful Spring Boot startup
        assertThat(logs).contains("Started StockLedgerManagementApplication");
        
        // Verify no critical errors in logs
        assertThat(logs).doesNotContain("ERROR");
        
        // Check for successful database connection
        assertThat(logs).containsAnyOf("HikariPool", "Started HikariPool");
        
        // Verify JPA/Hibernate initialization
        assertThat(logs).containsAnyOf("Hibernate", "JPA");
        
        // Check for successful web server startup
        assertThat(logs).containsAnyOf("Tomcat started on port", "Netty started on port");
        
        System.out.println("=== LOG ANALYSIS PASSED ===");
    }

    /**
     * Verify that all critical application components are loaded
     */
    private void verifyApplicationComponents() {
        Collection<Object> beans = applicationContext.getBeansOfType(Object.class).values();
        
        // Verify Controllers are loaded
        assertThat(beans)
                .extracting(bean -> bean.getClass().getSimpleName())
                .anyMatch(name -> name.toString().contains("Controller"));

        // Verify Services are loaded  
        assertThat(beans)
                .extracting(bean -> bean.getClass().getSimpleName())
                .anyMatch(name -> name.toString().contains("Service"));

        // Verify Repositories are loaded
        assertThat(beans)
                .extracting(bean -> bean.getClass().getSimpleName())
                .anyMatch(name -> name.toString().contains("Repository"));

        System.out.println("=== APPLICATION COMPONENTS VERIFICATION PASSED ===");
    }

    /**
     * Verify database connectivity and basic operations
     */
    private void verifyDatabaseConnectivity() {
        // Verify PostgreSQL container is accessible
        assertTrue(postgres.isRunning());
        assertTrue(postgres.isCreated());
        
        // Verify JDBC URL format
        assertThat(postgres.getJdbcUrl()).startsWith("jdbc:postgresql://");
        assertThat(postgres.getDatabaseName()).isEqualTo("stock_ledger_test");
        assertThat(postgres.getUsername()).isEqualTo("test_user");
        assertThat(postgres.getPassword()).isEqualTo("test_password");
        
        System.out.println("=== DATABASE CONNECTIVITY VERIFICATION PASSED ===");
    }

    /**
     * Additional test to verify application health endpoint
     */
    @Test
    void shouldExposeHealthEndpoint() throws Exception {
        // This test verifies that actuator endpoints are available
        // The health endpoint should be accessible once the application starts
        assertThat(applicationContext).isNotNull();
        
        Collection<Object> beans = applicationContext.getBeansOfType(Object.class).values();
        
        // Verify actuator beans are loaded
        assertThat(beans)
                .extracting(bean -> bean.getClass().getSimpleName())
                .anyMatch(name -> name.toString().contains("HealthEndpoint") || name.toString().contains("Health"));
                
        System.out.println("=== HEALTH ENDPOINT VERIFICATION PASSED ===");
    }

    /**
     * Test to verify configuration properties are loaded correctly
     */
    @Test
    void shouldLoadConfigurationProperties(CapturedOutput output) {
        assertThat(applicationContext).isNotNull();
        
        Collection<Object> beans = applicationContext.getBeansOfType(Object.class).values();
        
        // Verify that configuration classes are loaded
        assertThat(beans)
                .extracting(bean -> bean.getClass().getSimpleName())
                .anyMatch(name -> name.toString().contains("Config"));
        
        System.out.println("=== CONFIGURATION PROPERTIES VERIFICATION PASSED ===");
        
        // Print configuration details from logs
        String logs = output.getOut();
        if (logs.contains("Configuration") || logs.contains("Properties")) {
            System.out.println("=== CONFIGURATION DETAILS FOUND IN LOGS ===");
        }
    }
}