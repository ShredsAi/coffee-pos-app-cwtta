package ai.shreds.adapter.primary;

import ai.shreds.application.ports.ApplicationInputPortMediaService;
import ai.shreds.shared.dtos.SharedMediaUploadRequest;
import ai.shreds.shared.dtos.SharedMediaUpdateRequest;
import ai.shreds.shared.dtos.SharedMediaReorderRequest;
import ai.shreds.shared.dtos.SharedProductMediaDTO;
import ai.shreds.shared.dtos.ApplicationUploadMediaCommand;
import ai.shreds.shared.dtos.ApplicationUpdateMediaCommand;
import ai.shreds.shared.dtos.ApplicationReorderMediaCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products/{productId}/media")
@Tag(name = "Media", description = "Product media management operations")
public class AdapterMediaController {

    private final ApplicationInputPortMediaService applicationMediaService;

    public AdapterMediaController(ApplicationInputPortMediaService applicationMediaService) {
        this.applicationMediaService = applicationMediaService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload media", description = "Uploads a media file for a product (image, video, or document)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Media uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or file size too large"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "415", description = "Unsupported media type")
    })
    public ResponseEntity<SharedProductMediaDTO> uploadMedia(
            @Parameter(description = "ID of the product to upload media for") @PathVariable UUID productId,
            @Parameter(description = "Media file to upload") @RequestPart("file") MultipartFile file,
            @Valid @RequestPart SharedMediaUploadRequest request) {
        ApplicationUploadMediaCommand command = ApplicationUploadMediaCommand.fromRequest(productId, file, request);
        SharedProductMediaDTO dto = applicationMediaService.uploadMedia(command);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @PutMapping("/{mediaId}")
    @Operation(summary = "Update media metadata", description = "Updates metadata of an existing media file")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Media updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Product or media not found"),
        @ApiResponse(responseCode = "409", description = "Version conflict")
    })
    public ResponseEntity<SharedProductMediaDTO> updateMedia(
            @Parameter(description = "ID of the product") @PathVariable UUID productId,
            @Parameter(description = "ID of the media to update") @PathVariable UUID mediaId,
            @Valid @RequestBody SharedMediaUpdateRequest request) {
        ApplicationUpdateMediaCommand command = ApplicationUpdateMediaCommand.fromRequest(productId, mediaId, request);
        SharedProductMediaDTO dto = applicationMediaService.updateMedia(command);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{mediaId}")
    @Operation(summary = "Get media by ID", description = "Returns a single media file's metadata")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Media found",
                    content = @Content(schema = @Schema(implementation = SharedProductMediaDTO.class))),
        @ApiResponse(responseCode = "404", description = "Product or media not found")
    })
    public ResponseEntity<SharedProductMediaDTO> getMedia(
            @Parameter(description = "ID of the product") @PathVariable UUID productId,
            @Parameter(description = "ID of the media to retrieve") @PathVariable UUID mediaId) {
        SharedProductMediaDTO dto = applicationMediaService.getMedia(productId, mediaId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    @Operation(summary = "List product media", description = "Returns all media files for a product ordered by sort order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Media list retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<List<SharedProductMediaDTO>> listProductMedia(
            @Parameter(description = "ID of the product") @PathVariable UUID productId) {
        List<SharedProductMediaDTO> list = applicationMediaService.listProductMedia(productId);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{mediaId}")
    @Operation(summary = "Delete media", description = "Deletes a media file and removes it from storage")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Media deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Product or media not found")
    })
    public ResponseEntity<Void> deleteMedia(
            @Parameter(description = "ID of the product") @PathVariable UUID productId,
            @Parameter(description = "ID of the media to delete") @PathVariable UUID mediaId) {
        applicationMediaService.deleteMedia(productId, mediaId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reorder")
    @Operation(summary = "Reorder media", description = "Changes the sort order and primary media selection")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Media reordered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid media order data"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<List<SharedProductMediaDTO>> reorderMedia(
            @Parameter(description = "ID of the product") @PathVariable UUID productId,
            @Valid @RequestBody SharedMediaReorderRequest request) {
        ApplicationReorderMediaCommand command = ApplicationReorderMediaCommand.fromRequest(productId, request);
        List<SharedProductMediaDTO> list = applicationMediaService.reorderMedia(command);
        return ResponseEntity.ok(list);
    }
}