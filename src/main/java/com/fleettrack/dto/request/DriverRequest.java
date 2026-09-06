package com.fleettrack.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Payload for creating or updating a driver profile")
public class DriverRequest {

    @Schema(description = "Driver's first name", example = "Ali")
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Schema(description = "Driver's last name", example = "Mammadov")
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Schema(description = "Official driver's license number", example = "AZ12345678")
    @NotBlank(message = "License number is required")
    @Size(max = 50, message = "License number must not exceed 50 characters")
    private String licenseNumber;

    @Schema(description = "Contact details of the driver")
    @Valid
    @NotNull(message = "Contact data is required")
    private ContactDataRequest contactData;

    @Schema(description = "ID of the vehicle assigned to this driver", example = "1")
    private Long assignedVehicleId;

    @Schema(description = "ID of the User account linked to this driver profile", example = "15")
    @NotNull(message = "User ID is required to link the profile with an account")
    private Long userId;
}