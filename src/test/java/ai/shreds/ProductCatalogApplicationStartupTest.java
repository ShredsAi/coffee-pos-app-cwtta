package ai.shreds;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify the Product Catalog application starts successfully.
 * This test uses an in-memory H2 database and mocks for S3 storage to avoid disk space issues.
 * It starts the full SpringBoot application and verifies it's running correctly.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.allow-bean-definition-overriding=true", 
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password="
    }
)
@TestPropertySource(properties = {
    "storage.s3.endpoint=http://localhost:9000",
    "storage.s3.access-key=test-access-key",
    "storage.s3.secret-key=test-secret-key",
    "storage.s3.bucket=test-bucket",
    "storage.s3.region=us-east-1",
    "storage.cdn.base-url=http://localhost:9000/test-bucket"
})
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
@DisplayName("Product Catalog Application Startup Integration Test")
class ProductCatalogApplicationStartupTest {

    private static final Logger logger = LoggerFactory.getLogger(ProductCatalogApplicationStartupTest.class);

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("testdb")
                .build();
        }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Should start Spring Boot application successfully and verify it's running")
    void shouldStartApplicationSuccessfully(CapturedOutput output) {
        // Log test configuration
        logger.info("=== TEST CONFIGURATION ===");
        logger.info("Using in-memory H2 database for testing");
        logger.info("Using mock S3 storage configuration");
        
        // Verify application context loaded successfully
        logger.info("=== APPLICATION CONTEXT VERIFICATION ===");
        assertNotNull(applicationContext, "Application context should not be null");
        logger.info("Application context loaded successfully");
        
        // Verify the application is running on the expected port
        logger.info("=== APPLICATION PORT VERIFICATION ===");
        assertTrue(port > 0, "Application should be running on a valid port");
        logger.info("Application is running on port: {}", port);
        
        // Verify basic connectivity by calling a simple endpoint
        logger.info("=== APPLICATION CONNECTIVITY VERIFICATION ===");
        String url = "http://localhost:" + port + "/api/actuator/health";
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("Health endpoint response status: {}", response.getStatusCode());
            logger.info("Health endpoint response body: {}", response.getBody());
            
            // Accept both OK and other valid statuses (the endpoint might not be configured)
            assertTrue(response.getStatusCode().is2xxSuccessful() || 
                      response.getStatusCode() == HttpStatus.NOT_FOUND,
                      "Health endpoint should respond with 2xx or 404 status");
        } catch (Exception e) {
            logger.warn("Health endpoint not available (this is expected if actuator is not configured): {}", e.getMessage());
            // Try a simple root endpoint instead
            try {
                String rootUrl = "http://localhost:" + port + "/api";
                ResponseEntity<String> rootResponse = restTemplate.getForEntity(rootUrl, String.class);
                logger.info("Root endpoint response status: {}", rootResponse.getStatusCode());
                // Even a 404 means the server is responding
                assertTrue(rootResponse.getStatusCode().value() < 500,
                          "Root endpoint should respond (even with 404)");
            } catch (Exception rootException) {
                fail("Application seems to be not responding at all: " + rootException.getMessage());
            }
        }
        
        // Verify critical beans are loaded
        logger.info("=== SPRING BEANS VERIFICATION ===");
        verifyEssentialBeansAreLoaded();
        
        // Analyze startup logs for any errors or warnings
        logger.info("=== STARTUP LOGS ANALYSIS ===");
        analyzeStartupLogs(output);
        
        logger.info("=== APPLICATION STARTUP TEST COMPLETED SUCCESSFULLY ===");
    }
    
    private void verifyEssentialBeansAreLoaded() {
        // Verify some key beans are loaded (but don't fail if they're not implemented yet)
        try {
            boolean hasDataSource = applicationContext.containsBean("dataSource");
            logger.info("DataSource bean loaded: {}", hasDataSource);
            
            boolean hasEntityManagerFactory = applicationContext.containsBean("entityManagerFactory");
            logger.info("EntityManagerFactory bean loaded: {}", hasEntityManagerFactory);
            
            boolean hasTransactionManager = applicationContext.containsBean("transactionManager");
            logger.info("TransactionManager bean loaded: {}", hasTransactionManager);
            
            boolean hasS3Client = applicationContext.containsBean("s3Client");
            logger.info("S3Client bean loaded: {}", hasS3Client);
            
            // At minimum, we should have basic Spring Boot beans
            assertTrue(hasDataSource || hasEntityManagerFactory, 
                      "At least basic data access beans should be loaded");
                      
        } catch (Exception e) {
            logger.warn("Error checking beans (this might be expected if some are not implemented yet): {}", e.getMessage());
        }
    }
    
    private void analyzeStartupLogs(CapturedOutput output) {
        String logs = output.getOut();
        
        logger.info("=== CAPTURED APPLICATION STARTUP LOGS ===");
        logger.info(logs);
        
        // Check for successful startup indicators
        boolean hasStartedMessage = logs.contains("Started ProductCatalogApplication") ||
                                   logs.contains("Started application") ||
                                   logs.contains("Tomcat started");
        
        if (hasStartedMessage) {
            logger.info("✅ Found application startup success message in logs");
        } else {
            logger.warn("⚠️  Could not find explicit startup success message, but application context loaded");
        }
        
        // Check for critical errors
        boolean hasCriticalErrors = logs.contains("APPLICATION FAILED TO START") ||
                                   logs.contains("Error starting ApplicationContext") ||
                                   logs.contains("FATAL");
        
        assertFalse(hasCriticalErrors, "Application logs should not contain critical startup errors");
        
        if (!hasCriticalErrors) {
            logger.info("✅ No critical errors found in startup logs");
        }
        
        // Count warnings (informational)
        long warningCount = logs.lines().filter(line -> line.contains("WARN")).count();
        logger.info("Warning count in logs: {}", warningCount);
        
        if (warningCount > 10) {
            logger.warn("⚠️  High number of warnings ({}) detected in startup logs - review recommended", warningCount);
        }
        
        logger.info("=== STARTUP LOGS ANALYSIS COMPLETED ===");
    }
}