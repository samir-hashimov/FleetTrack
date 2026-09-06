package com.fleettrack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FleetAlertMessage {

    private String type;
    private String message;
    private Long vehicleId;
    private String licensePlate;
    private Instant timestamp;
}
