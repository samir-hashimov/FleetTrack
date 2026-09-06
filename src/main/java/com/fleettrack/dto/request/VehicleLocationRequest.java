package com.fleettrack.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload for transmitting real-time GPS coordinates")
public class VehicleLocationRequest {

    @Schema(description = "ID of the vehicle", example = "5")
    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @Schema(description = "Current latitude coordinate", example = "40.409264")
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double latitude;

    @Schema(description = "Current longitude coordinate", example = "49.867092")
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double longitude;
}