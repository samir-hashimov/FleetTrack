package com.fleettrack.websocket;

import com.fleettrack.dto.response.FleetAlertMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FleetAlertWebSocketBroadcaster {

    public static final String ALERTS_TOPIC = "/topic/fleet-alerts";

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcast(FleetAlertMessage alert) {
        messagingTemplate.convertAndSend(ALERTS_TOPIC, alert);
    }
}
