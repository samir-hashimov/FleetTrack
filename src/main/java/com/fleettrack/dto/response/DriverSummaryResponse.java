package com.fleettrack.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Summary response payload for a driver profile, typically used in lists or dropdown menus")
public class DriverSummaryResponse {

    @Schema(description = "Driver profile unique ID", example = "1")
    private Long id;

    @Schema(description = "Driver's full name (First and Last name combined)", example = "Ali Mammadov")
    private String fullName;

    @Schema(description = "Official driver's license number", example = "AZ12345678")
    private String licenseNumber;

    @Schema(description = "License plate of the assigned vehicle (if any)", example = "99-AA-999")
    private String assignedVehiclePlate;
}