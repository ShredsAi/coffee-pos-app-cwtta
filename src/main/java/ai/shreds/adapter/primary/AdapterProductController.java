package ai.shreds.adapter.primary;

import ai.shreds.application.ports.ApplicationInputPortProductService;
import ai.shreds.shared.dtos.SharedProductCreateRequest;
import ai.shreds.shared.dtos.SharedProductDTO;
import ai.shreds.shared.dtos.SharedProductUpdateRequest;
import ai.shreds.shared.dtos.SharedPagedResponse;
import ai.shreds.shared.dtos.ApplicationCreateProductCommand;
import ai.shreds.shared.dtos.ApplicationUpdateProductCommand;
import ai.shreds.shared.value_objects.SharedProductFilterParams;
import ai.shreds.application.services.ApplicationProductFilterSpecification;
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
@RequestMapping("/products")
@Tag(name = "Products", description = "Product catalog management operations")
public class AdapterProductController {

    private final ApplicationInputPortProductService applicationProductService;

    public AdapterProductController(ApplicationInputPortProductService applicationProductService) {
        this.applicationProductService = applicationProductService;
    }

    @PostMapping
    @Operation(summary = "Create a new product", description = "Creates a new product with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Product with same SKU or slug already exists")
    })
    public ResponseEntity<SharedProductDTO> createProduct(
            @Valid @RequestBody SharedProductCreateRequest request) {
        ApplicationCreateProductCommand command = ApplicationCreateProductCommand.fromRequest(request);
        SharedProductDTO result = applicationProductService.createProduct(command);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product", description = "Updates a product with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "409", description = "Version conflict or duplicate SKU/slug")
    })
    public ResponseEntity<SharedProductDTO> updateProduct(
            @Parameter(description = "ID of the product to update") @PathVariable UUID id,
            @Valid @RequestBody SharedProductUpdateRequest request) {
        ApplicationUpdateProductCommand command = ApplicationUpdateProductCommand.fromRequest(id, request);
        SharedProductDTO result = applicationProductService.updateProduct(command);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by ID", description = "Returns a single product identified by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = SharedProductDTO.class))),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<SharedProductDTO> getProduct(
            @Parameter(description = "ID of the product to retrieve") @PathVariable UUID id) {
        SharedProductDTO result = applicationProductService.getProduct(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @Operation(summary = "List products", description = "Returns a paginated list of products with optional filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters")
    })
    public ResponseEntity<SharedPagedResponse<SharedProductDTO>> listProducts(
            @Valid SharedProductFilterParams filterParams) {
        ApplicationProductFilterSpecification specification = ApplicationProductFilterSpecification.fromFilterParams(filterParams);
        SharedPagedResponse<SharedProductDTO> page = applicationProductService.listProducts(specification);
        return ResponseEntity.ok(page);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product", description = "Deletes a product identified by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID of the product to delete") @PathVariable UUID id) {
        applicationProductService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}