package com.fleettrack.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Payload for refreshing access token")
public class RefreshTokenRequest {
    
    @Schema(description = "Valid refresh token string", example = "eyJhbGciOiJIUzI1NiJ9...")
    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;
}