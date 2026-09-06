package com.fleettrack.websocket;

import com.fleettrack.dto.request.VehicleLocationRequest;
import com.fleettrack.service.VehicleLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class VehicleGpsWebSocketController {

    private final VehicleLocationService vehicleLocationService;

    @MessageMapping("/gps")
    public void ingestGps(@Valid @Payload VehicleLocationRequest request) {
        vehicleLocationService.updateLocation(request);
    }
}
