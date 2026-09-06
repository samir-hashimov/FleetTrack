package com.fleettrack.dto.response;

import com.fleettrack.util.DriverStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Detailed response payload for a driver profile")
public class DriverResponse {

    @Schema(description = "Driver profile unique ID", example = "1")
    private Long id;

    @Schema(description = "ID of the User account linked to this profile", example = "15")
    private Long userId;

    @Schema(description = "Driver's first name", example = "Ali")
    private String firstName;

    @Schema(description = "Driver's last name", example = "Mammadov")
    private String lastName;

    @Schema(description = "Official driver's license number", example = "AZ12345678")
    private String licenseNumber;

    @Schema(description = "Driver's contact information")
    private ContactDataResponse contactData;

    @Schema(description = "ID of the assigned vehicle (if any)", example = "5")
    private Long assignedVehicleId;

    @Schema(description = "License plate of the assigned vehicle", example = "99-AA-999")
    private String assignedVehiclePlate;

    @Schema(description = "Current status of the driver", example = "ACTIVE")
    private DriverStatus status;

    @Schema(description = "Record creation timestamp")
    private Instant createdAt;

    @Schema(description = "Record last update timestamp")
    private Instant updatedAt;
}