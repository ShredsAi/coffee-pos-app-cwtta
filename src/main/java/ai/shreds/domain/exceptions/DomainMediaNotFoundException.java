package ai.shreds.domain.exceptions;

import java.util.UUID;

/**
 * Domain Media Not Found Exception
 * Thrown when a media item is not found by ID
 */
public class DomainMediaNotFoundException extends RuntimeException {
    
    private final UUID mediaId;
    
    /**
     * Constructor with media ID
     * 
     * @param mediaId The media ID that was not found
     */
    public DomainMediaNotFoundException(UUID mediaId) {
        super(String.format("Media with ID '%s' not found", mediaId));
        this.mediaId = mediaId;
    }
    
    /**
     * Gets the media ID that was not found
     * 
     * @return The media ID
     */
    public UUID getMediaId() {
        return mediaId;
    }
}