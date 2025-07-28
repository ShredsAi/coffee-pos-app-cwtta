package ai.shreds.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedErrorResponse {
    
    private Instant timestamp;
    
    private Integer status;
    
    private String error;
    
    private String message;
    
    private String path;
    
    private Map<String, String> validationErrors;
    
    public static SharedErrorResponse badRequest(String message, String path) {
        return SharedErrorResponse.builder()
                .timestamp(Instant.now())
                .status(400)
                .error("Bad Request")
                .message(message)
                .path(path)
                .build();
    }
    
    public static SharedErrorResponse notFound(String message, String path) {
        return SharedErrorResponse.builder()
                .timestamp(Instant.now())
                .status(404)
                .error("Not Found")
                .message(message)
                .path(path)
                .build();
    }
    
    public static SharedErrorResponse conflict(String message, String path) {
        return SharedErrorResponse.builder()
                .timestamp(Instant.now())
                .status(409)
                .error("Conflict")
                .message(message)
                .path(path)
                .build();
    }
    
    public static SharedErrorResponse validationError(Map<String, String> errors, String path) {
        return SharedErrorResponse.builder()
                .timestamp(Instant.now())
                .status(400)
                .error("Validation Error")
                .message("Validation failed")
                .path(path)
                .validationErrors(errors)
                .build();
    }
    
    public static SharedErrorResponse internalError(String message, String path) {
        return SharedErrorResponse.builder()
                .timestamp(Instant.now())
                .status(500)
                .error("Internal Server Error")
                .message(message)
                .path(path)
                .build();
    }
}