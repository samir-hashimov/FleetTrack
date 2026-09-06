package com.fleettrack.websocket;

import com.fleettrack.dto.response.VehicleLocationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VehicleLocationBroadcaster {

    public static final String GPS_TOPIC = "/topic/vehicle-locations";

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcast(VehicleLocationResponse location) {
        messagingTemplate.convertAndSend(GPS_TOPIC, location);
    }
}
