package com.fleettrack.controller;

import com.fleettrack.dto.request.VehicleLocationRequest;
import com.fleettrack.dto.response.VehicleLocationResponse;
import com.fleettrack.service.VehicleLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Vehicle Locations", description = "GPS tracking and WebSocket broadcast endpoints")
@RestController
@RequestMapping("/vehicle-locations")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class VehicleLocationController {

    private final VehicleLocationService vehicleLocationService;

    @Operation(summary = "Update vehicle coordinates and broadcast to live map")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location updated and broadcast via WebSocket",
                    content = @Content(schema = @Schema(implementation = VehicleLocationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Can only update assigned vehicle", content = @Content),
            @ApiResponse(responseCode = "404", description = "Vehicle not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER', 'DRIVER')")
    @PostMapping
    public ResponseEntity<VehicleLocationResponse> updateLocation(@Valid @RequestBody VehicleLocationRequest request) {
        return ResponseEntity.ok(vehicleLocationService.updateLocation(request));
    }

    @Operation(summary = "Mark a vehicle as offline and trigger Redis fleet alert")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle marked offline successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Can only update assigned vehicle", content = @Content),
            @ApiResponse(responseCode = "404", description = "Vehicle not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER', 'DRIVER')")
    @PostMapping("/{vehicleId}/offline")
    public ResponseEntity<Void> markOffline(@PathVariable Long vehicleId) {
        vehicleLocationService.markVehicleOffline(vehicleId);
        return ResponseEntity.ok().build();
    }
}