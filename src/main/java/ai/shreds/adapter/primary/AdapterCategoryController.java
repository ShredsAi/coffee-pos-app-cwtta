package ai.shreds.adapter.primary;

import ai.shreds.application.ports.ApplicationInputPortCategoryService;
import ai.shreds.shared.dtos.SharedCategoryCreateRequest;
import ai.shreds.shared.dtos.SharedCategoryDTO;
import ai.shreds.shared.dtos.SharedCategoryUpdateRequest;
import ai.shreds.shared.dtos.SharedPagedResponse;
import ai.shreds.shared.dtos.ApplicationCreateCategoryCommand;
import ai.shreds.shared.dtos.ApplicationUpdateCategoryCommand;
import ai.shreds.shared.value_objects.SharedCategoryFilterParams;
import ai.shreds.application.services.ApplicationCategoryFilterSpecification;
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
@RequestMapping("/categories")
@Tag(name = "Categories", description = "Product category management operations")
public class AdapterCategoryController {

    private final ApplicationInputPortCategoryService applicationCategoryService;

    public AdapterCategoryController(ApplicationInputPortCategoryService applicationCategoryService) {
        this.applicationCategoryService = applicationCategoryService;
    }

    @PostMapping
    @Operation(summary = "Create a new category", description = "Creates a new category with hierarchical path calculation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Category created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Category with same slug already exists")
    })
    public ResponseEntity<SharedCategoryDTO> createCategory(
            @Valid @RequestBody SharedCategoryCreateRequest request) {
        ApplicationCreateCategoryCommand command = ApplicationCreateCategoryCommand.fromRequest(request);
        SharedCategoryDTO result = applicationCategoryService.createCategory(command);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing category", description = "Updates a category and recalculates hierarchy if needed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "409", description = "Version conflict or circular reference")
    })
    public ResponseEntity<SharedCategoryDTO> updateCategory(
            @Parameter(description = "ID of the category to update") @PathVariable UUID id,
            @Valid @RequestBody SharedCategoryUpdateRequest request) {
        ApplicationUpdateCategoryCommand command = ApplicationUpdateCategoryCommand.fromRequest(id, request);
        SharedCategoryDTO result = applicationCategoryService.updateCategory(command);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a category by ID", description = "Returns a single category identified by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category found",
                    content = @Content(schema = @Schema(implementation = SharedCategoryDTO.class))),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<SharedCategoryDTO> getCategory(
            @Parameter(description = "ID of the category to retrieve") @PathVariable UUID id) {
        SharedCategoryDTO result = applicationCategoryService.getCategory(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @Operation(summary = "List categories", description = "Returns a paginated list of categories with optional filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters")
    })
    public ResponseEntity<SharedPagedResponse<SharedCategoryDTO>> listCategories(
            @Valid SharedCategoryFilterParams filterParams) {
        ApplicationCategoryFilterSpecification specification = ApplicationCategoryFilterSpecification.fromFilterParams(filterParams);
        SharedPagedResponse<SharedCategoryDTO> page = applicationCategoryService.listCategories(specification);
        return ResponseEntity.ok(page);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category", description = "Deletes a category identified by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "409", description = "Category has active child categories or products")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID of the category to delete") @PathVariable UUID id) {
        applicationCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}