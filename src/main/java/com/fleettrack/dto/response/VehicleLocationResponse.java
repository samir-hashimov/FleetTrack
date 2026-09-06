package com.fleettrack.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload containing the updated location broadcasted via WebSocket")
public class VehicleLocationResponse {

    @Schema(description = "ID of the vehicle", example = "5")
    private Long vehicleId;

    @Schema(description = "License plate of the vehicle", example = "99-AA-999")
    private String licensePlate;

    @Schema(description = "Updated latitude coordinate", example = "40.409264")
    private Double latitude;

    @Schema(description = "Updated longitude coordinate", example = "49.867092")
    private Double longitude;

    @Schema(description = "Exact timestamp of the location update")
    private Instant timestamp;
}