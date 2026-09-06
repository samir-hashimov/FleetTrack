package com.fleettrack.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Detailed response payload for a maintenance log entry")
public class MaintenanceLogResponse {

    @Schema(description = "Unique ID of the maintenance log", example = "1")
    private Long id;

    @Schema(description = "ID of the associated vehicle", example = "5")
    private Long vehicleId;

    @Schema(description = "License plate of the vehicle", example = "99-AA-999")
    private String vehicleLicensePlate;

    @Schema(description = "Details of the maintenance work", example = "Oil change and tire rotation")
    private String description;

    @Schema(description = "Scheduled date for the maintenance", example = "2026-09-15")
    private LocalDate scheduledDate;

    @Schema(description = "Indicates whether the maintenance has been completed", example = "true")
    private boolean completed;

    @Schema(description = "Record creation timestamp")
    private Instant createdAt;

    @Schema(description = "Record last update timestamp")
    private Instant updatedAt;
}