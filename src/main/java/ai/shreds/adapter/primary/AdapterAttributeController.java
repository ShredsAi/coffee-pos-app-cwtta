package ai.shreds.adapter.primary;

import ai.shreds.application.ports.ApplicationInputPortAttributeService;
import ai.shreds.shared.dtos.SharedAttributeCreateRequest;
import ai.shreds.shared.dtos.SharedAttributeUpdateRequest;
import ai.shreds.shared.dtos.SharedProductAttributeDTO;
import ai.shreds.shared.dtos.SharedAttributeOptionCreateRequest;
import ai.shreds.shared.dtos.SharedAttributeOptionDTO;
import ai.shreds.shared.dtos.SharedPagedResponse;
import ai.shreds.shared.dtos.ApplicationCreateAttributeCommand;
import ai.shreds.shared.dtos.ApplicationUpdateAttributeCommand;
import ai.shreds.shared.dtos.ApplicationCreateAttributeOptionCommand;
import ai.shreds.shared.value_objects.SharedAttributeFilterParams;
import ai.shreds.application.services.ApplicationAttributeFilterSpecification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/attributes")
@Tag(name = "Attributes", description = "Product attribute management operations")
public class AdapterAttributeController {

    private final ApplicationInputPortAttributeService applicationAttributeService;

    public AdapterAttributeController(ApplicationInputPortAttributeService applicationAttributeService) {
        this.applicationAttributeService = applicationAttributeService;
    }

    @PostMapping
    @Operation(summary = "Create a new attribute", description = "Creates a new product attribute definition with options")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Attribute created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Attribute with same code already exists")
    })
    public ResponseEntity<SharedProductAttributeDTO> createAttribute(
            @Valid @RequestBody SharedAttributeCreateRequest request) {
        ApplicationCreateAttributeCommand command = ApplicationCreateAttributeCommand.fromRequest(request);
        SharedProductAttributeDTO dto = applicationAttributeService.createAttribute(command);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing attribute", description = "Updates a product attribute definition")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attribute updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Attribute not found"),
        @ApiResponse(responseCode = "409", description = "Version conflict")
    })
    public ResponseEntity<SharedProductAttributeDTO> updateAttribute(
            @Parameter(description = "ID of the attribute to update") @PathVariable UUID id,
            @Valid @RequestBody SharedAttributeUpdateRequest request) {
        ApplicationUpdateAttributeCommand command = ApplicationUpdateAttributeCommand.fromRequest(id, request);
        SharedProductAttributeDTO dto = applicationAttributeService.updateAttribute(command);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an attribute by ID", description = "Returns a single attribute definition with its options")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attribute found",
                    content = @Content(schema = @Schema(implementation = SharedProductAttributeDTO.class))),
        @ApiResponse(responseCode = "404", description = "Attribute not found")
    })
    public ResponseEntity<SharedProductAttributeDTO> getAttribute(
            @Parameter(description = "ID of the attribute to retrieve") @PathVariable UUID id) {
        SharedProductAttributeDTO dto = applicationAttributeService.getAttribute(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    @Operation(summary = "List attributes", description = "Returns a paginated list of product attributes with optional filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attributes retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters")
    })
    public ResponseEntity<SharedPagedResponse<SharedProductAttributeDTO>> listAttributes(
            @Valid SharedAttributeFilterParams filterParams) {
        ApplicationAttributeFilterSpecification specification = ApplicationAttributeFilterSpecification.fromFilterParams(filterParams);
        SharedPagedResponse<SharedProductAttributeDTO> page = applicationAttributeService.listAttributes(specification);
        return ResponseEntity.ok(page);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an attribute", description = "Deletes a product attribute definition identified by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Attribute deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Attribute not found"),
        @ApiResponse(responseCode = "409", description = "Attribute is in use by products")
    })
    public ResponseEntity<Void> deleteAttribute(
            @Parameter(description = "ID of the attribute to delete") @PathVariable UUID id) {
        applicationAttributeService.deleteAttribute(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{attributeId}/options")
    @Operation(summary = "Add attribute option", description = "Adds a new option to a select-type attribute")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Option created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Attribute not found"),
        @ApiResponse(responseCode = "409", description = "Option with same code already exists in attribute")
    })
    public ResponseEntity<SharedAttributeOptionDTO> addAttributeOption(
            @Parameter(description = "ID of the attribute to add option to") @PathVariable UUID attributeId,
            @Valid @RequestBody SharedAttributeOptionCreateRequest request) {
        ApplicationCreateAttributeOptionCommand command = ApplicationCreateAttributeOptionCommand.fromRequest(attributeId, request);
        SharedAttributeOptionDTO dto = applicationAttributeService.addAttributeOption(command);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }
}