package com.fleettrack.dto.response;

import com.fleettrack.util.VehicleStatus;
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
public class VehicleSummaryResponse {

    private Long id;
    private String make;
    private String model;
    private String licensePlate;
    private VehicleStatus status;
}
