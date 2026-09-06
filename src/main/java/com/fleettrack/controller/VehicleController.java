package com.fleettrack.controller;

import com.fleettrack.dto.request.VehicleRequest;
import com.fleettrack.dto.response.PageResponse;
import com.fleettrack.dto.response.VehicleResponse;
import com.fleettrack.dto.response.VehicleSummaryResponse;
import com.fleettrack.service.VehicleService;
import com.fleettrack.util.VehicleStatus;
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

@Tag(name = "Vehicles", description = "Vehicle fleet management endpoints")
@RestController
@RequestMapping("/vehicles")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @Operation(summary = "Create a new vehicle (Admin & Manager only)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vehicle created", content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation or business error", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @PostMapping
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.create(request));
    }

    @Operation(summary = "Search vehicles with filtering, sorting, and pagination")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @GetMapping
    public ResponseEntity<PageResponse<VehicleResponse>> search(
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(required = false) Integer minYear,
            @RequestParam(required = false) Integer maxYear,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Boolean hasAssignedDriver,
            @RequestParam(required = false) String licensePlate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(vehicleService.search(status, minYear, maxYear, driverId, hasAssignedDriver, licensePlate, pageable));
    }

    @Operation(summary = "Get cached vehicle summary list")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @GetMapping("/summaries")
    public ResponseEntity<List<VehicleSummaryResponse>> getSummaries() {
        return ResponseEntity.ok(vehicleService.getSummaries());
    }

    @Operation(summary = "Get vehicle by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER', 'DRIVER')")
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getById(id));
    }

    @Operation(summary = "Update an existing vehicle (Admin & Manager only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> update(@PathVariable Long id, @Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.ok(vehicleService.update(id, request));
    }

    @Operation(summary = "Delete a vehicle (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}