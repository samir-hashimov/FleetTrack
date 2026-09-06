package com.fleettrack.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload for creating or updating a maintenance log entry")
public class MaintenanceLogRequest {

    @Schema(description = "ID of the vehicle undergoing maintenance", example = "5")
    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @Schema(description = "Details of the maintenance work", example = "Oil change and tire rotation")
    @NotBlank(message = "Description is required")
    private String description;

    @Schema(description = "Scheduled date for the maintenance", example = "2026-09-15")
    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;

    @Schema(description = "Indicates whether the maintenance has been completed", example = "false")
    private boolean completed;
}