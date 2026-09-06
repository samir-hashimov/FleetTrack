package com.fleettrack.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Response payload containing authentication tokens and user details")
public class AuthResponse {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Username", example = "driver_ali")
    private String username;

    @Schema(description = "Assigned role", example = "DRIVER")
    private String role;

    @Schema(description = "JWT Access Token for API requests")
    private String accessToken;

    @Schema(description = "JWT Refresh Token for obtaining new access tokens")
    private String refreshToken;
}