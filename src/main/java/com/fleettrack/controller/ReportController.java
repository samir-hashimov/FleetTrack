package com.fleettrack.controller;

import com.fleettrack.service.FleetReportPdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reports", description = "Fleet reporting endpoints")
@RestController
@RequestMapping("/reports")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ReportController {

    private final FleetReportPdfService fleetReportPdfService;

    @Operation(summary = "Download fleet status and maintenance PDF report (Admin & Manager only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF report generated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or FLEET_MANAGER role"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @GetMapping("/fleet-status/pdf")
    public ResponseEntity<byte[]> downloadFleetStatusPdf() {
        byte[] pdf = fleetReportPdfService.generateFleetStatusReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fleet-status-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}