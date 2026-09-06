package com.fleettrack.controller;

import com.fleettrack.dto.request.ElevatedUserRequest;
import com.fleettrack.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Operations", description = "Role-restricted endpoints for system administrators")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService authService;

    @Operation(
            summary = "Create an elevated user",
            description = "Allows an existing ADMIN to create a new ADMIN or FLEET_MANAGER account. Cannot be used to create DRIVER accounts."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Elevated user created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or attempt to create a DRIVER role", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have ADMIN role", content = @Content),
            @ApiResponse(responseCode = "409", description = "Username already exists", content = @Content),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users")
    public ResponseEntity<Void> createElevatedUser(@Valid @RequestBody ElevatedUserRequest request) {
        authService.createElevatedUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}