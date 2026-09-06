package com.fleettrack.dto.request;

import com.fleettrack.util.VehicleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload for creating or updating a vehicle")
public class VehicleRequest {

    @Schema(description = "Vehicle manufacturer", example = "Toyota")
    @NotBlank(message = "Make is required")
    @Size(max = 100, message = "Make must not exceed 100 characters")
    private String make;

    @Schema(description = "Vehicle model", example = "Prius")
    @NotBlank(message = "Model is required")
    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;

    @Schema(description = "Manufacturing year", example = "2022")
    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be at least 1900")
    @Max(value = 2100, message = "Year must not exceed 2100")
    private Integer year;

    @Schema(description = "Official license plate", example = "99-AA-999")
    @NotBlank(message = "License plate is required")
    @Size(max = 20, message = "License plate must not exceed 20 characters")
    private String licensePlate;

    @Schema(description = "Current operational status", example = "ACTIVE")
    @NotNull(message = "Status is required")
    private VehicleStatus status;
}