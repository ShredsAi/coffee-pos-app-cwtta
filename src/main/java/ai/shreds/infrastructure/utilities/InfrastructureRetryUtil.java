package ai.shreds.infrastructure.utilities;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Utility for handling retries with external services.
 * Implements exponential backoff to handle transient failures.
 */
@Component
@Slf4j
public class InfrastructureRetryUtil {

    @Value("${services.retry.max-retries:3}")
    private Integer maxRetries;

    @Value("${services.retry.backoff-delay:500}")
    private Long backoffDelay;

    /**
     * Execute operation with retry for a specific number of times.
     * @param operation the operation to execute
     * @param maxRetries maximum number of retry attempts
     * @return the operation result
     * @param <T> the return type
     */
    public <T> T executeWithRetry(Supplier<T> operation, Integer maxRetries) {
        int attempts = 0;
        RuntimeException lastException = null;

        while (attempts < maxRetries) {
            try {
                return operation.get();
            } catch (RuntimeException e) {
                lastException = e;
                attempts++;
                log.warn("Retry attempt {} failed: {}", attempts, e.getMessage());

                if (attempts < maxRetries) {
                    try {
                        Thread.sleep(backoffDelay * (1L << (attempts - 1))); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }

        throw lastException != null ? lastException : 
                new RuntimeException("All retry attempts failed without specific exception");
    }

    /**
     * Execute operation with exponential backoff using default max retries.
     * @param operation the operation to execute
     * @return the operation result
     * @param <T> the return type
     */
    public <T> T executeWithExponentialBackoff(Supplier<T> operation) {
        return executeWithRetry(operation, maxRetries);
    }

    /**
     * Execute operation with no return value with retry.
     * @param runnable the operation to execute
     */
    public void executeWithRetry(Runnable runnable) {
        executeWithRetry(() -> {
            runnable.run();
            return null;
        }, maxRetries);
    }
}