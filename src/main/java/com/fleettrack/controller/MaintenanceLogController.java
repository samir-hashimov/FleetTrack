package com.fleettrack.controller;

import com.fleettrack.dto.request.MaintenanceLogRequest;
import com.fleettrack.dto.response.MaintenanceLogResponse;
import com.fleettrack.dto.response.PageResponse;
import com.fleettrack.service.MaintenanceLogService;
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

@Tag(name = "Maintenance Logs", description = "Vehicle maintenance and service history endpoints")
@RestController
@RequestMapping("/maintenance-logs")
@SecurityRequirement(name = "bearerAuth") // YENİ: Bütün metodlar JWT tələb edir
@RequiredArgsConstructor
public class MaintenanceLogController {

    private final MaintenanceLogService maintenanceLogService;

    @Operation(summary = "Create a maintenance log entry (Admin & Manager only)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Maintenance log created", content = @Content(schema = @Schema(implementation = MaintenanceLogResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')") // YENİ: Yalnız idarəçilər yarada bilər
    @PostMapping
    public ResponseEntity<MaintenanceLogResponse> create(@Valid @RequestBody MaintenanceLogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(maintenanceLogService.create(request));
    }

    @Operation(summary = "List all maintenance logs with pagination (Admin & Manager only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated maintenance logs retrieved")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @GetMapping
    public ResponseEntity<PageResponse<MaintenanceLogResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "scheduledDate") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(maintenanceLogService.getAll(pageable));
    }

    @Operation(summary = "List maintenance logs for a specific vehicle")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<MaintenanceLogResponse>> getByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(maintenanceLogService.getByVehicleId(vehicleId));
    }

    @Operation(summary = "Get maintenance log by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceLogResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceLogService.getById(id));
    }

    @Operation(summary = "Update a maintenance log entry (Admin & Manager only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceLogResponse> update(@PathVariable Long id, @Valid @RequestBody MaintenanceLogRequest request) {
        return ResponseEntity.ok(maintenanceLogService.update(id, request));
    }

    @Operation(summary = "Delete a maintenance log entry (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        maintenanceLogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}