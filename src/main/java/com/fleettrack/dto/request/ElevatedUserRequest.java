package com.fleettrack.dto.request;

import com.fleettrack.util.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Payload for creating elevated users (Admin/Fleet Manager)")
public class ElevatedUserRequest {

    @Schema(description = "Full name for the new elevated user", example = "John Doe")
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    private String username;

    @Schema(description = "Unique email address", example = "manager@fleettrack.com")
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Secure password", example = "SecurePass123!")
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Schema(description = "Role to assign (ADMIN or FLEET_MANAGER only)", example = "FLEET_MANAGER")
    @NotNull(message = "Role must be selected")
    private Role role;
}