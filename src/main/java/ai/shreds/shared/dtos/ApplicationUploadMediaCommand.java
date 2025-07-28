package ai.shreds.shared.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationUploadMediaCommand {
    
    @NotNull(message = "Product ID is required")
    private UUID productId;
    
    @NotNull(message = "File data is required")
    private byte[] file;
    
    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name must not exceed 255 characters")
    private String fileName;
    
    @NotBlank(message = "Content type is required")
    private String contentType;
    
    @Size(max = 255, message = "Alt text must not exceed 255 characters")
    private String altText;
    
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Builder.Default
    private Boolean isPrimary = false;
    
    @Min(value = 0, message = "Sort order must be non-negative")
    @Builder.Default
    private Integer sortOrder = 0;
    
    // TODO: Implementation of toDomainCommand() method will be added
    // after domain layer is available for proper transformation
    public Object toDomainCommand() {
        throw new UnsupportedOperationException("toDomainCommand() method will be implemented when domain layer is available");
    }
    
    public static ApplicationUploadMediaCommand fromRequest(UUID productId, MultipartFile file, SharedMediaUploadRequest request) {
        if (file == null || request == null) {
            return null;
        }
        
        try {
            return ApplicationUploadMediaCommand.builder()
                    .productId(productId)
                    .file(file.getBytes())
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .altText(request.getAltText())
                    .title(request.getTitle())
                    .isPrimary(request.getIsPrimary())
                    .sortOrder(request.getSortOrder())
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to process file upload", e);
        }
    }
}