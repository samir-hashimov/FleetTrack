package com.fleettrack.controller;

import com.fleettrack.dto.request.DriverRequest;
import com.fleettrack.dto.response.DriverResponse;
import com.fleettrack.dto.response.DriverSummaryResponse;
import com.fleettrack.dto.response.PageResponse;
import com.fleettrack.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Drivers", description = "Driver profile and status management endpoints")
@RestController
@RequestMapping("/drivers")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @Operation(summary = "Create a new driver profile (Admin & Manager only)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Driver created", content = @Content(schema = @Schema(implementation = DriverResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @PostMapping
    public ResponseEntity<DriverResponse> create(@Valid @RequestBody DriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(driverService.create(request));
    }

    @Operation(summary = "List all drivers with pagination (Admin & Manager only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of drivers retrieved")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @GetMapping
    public ResponseEntity<PageResponse<DriverResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(driverService.getAll(pageable));
    }

    @Operation(summary = "Get active driver summaries (Admin & Manager only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @GetMapping("/summaries")
    public ResponseEntity<List<DriverSummaryResponse>> getSummaries() {
        return ResponseEntity.ok(driverService.getSummaries());
    }

    @Operation(summary = "Get driver by ID (Drivers can only access their own ID)")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER', 'DRIVER')")
    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(driverService.getById(id));
    }

    @Operation(summary = "Update a driver (Drivers can only update their own profile)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot update another driver's profile or account is blocked", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER', 'DRIVER')")
    @PutMapping("/{id}")
    public ResponseEntity<DriverResponse> update(@PathVariable Long id, @Valid @RequestBody DriverRequest request) {
        return ResponseEntity.ok(driverService.update(id, request));
    }

    @Operation(summary = "Soft delete a driver (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Driver blocked and assigned vehicle removed"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only admins can perform deletions", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        driverService.delete(id);
        return ResponseEntity.noContent().build();
    }
}