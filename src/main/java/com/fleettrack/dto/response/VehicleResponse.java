package com.fleettrack.dto.response;

import com.fleettrack.util.VehicleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Detailed vehicle response payload")
public class VehicleResponse {

    @Schema(description = "Vehicle ID", example = "1")
    private Long id;

    @Schema(description = "Manufacturer", example = "Toyota")
    private String make;

    @Schema(description = "Model", example = "Prius")
    private String model;

    @Schema(description = "Manufacturing year", example = "2022")
    private Integer year;

    @Schema(description = "License plate", example = "99-AA-999")
    private String licensePlate;

    @Schema(description = "Current status", example = "ACTIVE")
    private VehicleStatus status;

    @Schema(description = "Last known latitude", example = "40.409264")
    private Double latitude;

    @Schema(description = "Last known longitude", example = "49.867092")
    private Double longitude;

    @Schema(description = "Assigned driver ID", example = "5")
    private Long assignedDriverId;

    @Schema(description = "Assigned driver's full name", example = "Ali Mammadov")
    private String assignedDriverName;

    private Instant createdAt;
    private Instant updatedAt;
}