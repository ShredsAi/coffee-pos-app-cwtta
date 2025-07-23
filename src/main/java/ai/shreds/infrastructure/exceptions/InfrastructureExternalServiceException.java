package ai.shreds.infrastructure.exceptions;

/**
 * Exception thrown when external service calls fail in the infrastructure layer.
 * Wraps HTTP client errors and provides context about the failed service call.
 */
public class InfrastructureExternalServiceException extends RuntimeException {

    private final String serviceName;
    private final int statusCode;

    /**
     * Constructs a new external service exception with details about the failed call.
     * @param message error message
     * @param serviceName the name of the external service that failed
     * @param statusCode the HTTP status code (0 if not HTTP-related)
     * @param cause the underlying exception
     */
    public InfrastructureExternalServiceException(String message, String serviceName, int statusCode, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }

    /**
     * Gets the name of the external service that failed.
     * @return service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Gets the HTTP status code from the failed response.
     * @return status code (0 if not HTTP-related)
     */
    public int getStatusCode() {
        return statusCode;
    }
}