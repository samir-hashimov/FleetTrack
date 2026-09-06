package com.fleettrack.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleettrack.dto.response.FleetAlertMessage;
import com.fleettrack.websocket.FleetAlertWebSocketBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class FleetAlertSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final FleetAlertWebSocketBroadcaster alertWebSocketBroadcaster;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            FleetAlertMessage alert = objectMapper.readValue(message.getBody(), FleetAlertMessage.class);
            log.info("Received fleet alert [{}]: {}", alert.getType(), alert.getMessage());
            alertWebSocketBroadcaster.broadcast(alert);
        } catch (Exception e) {
            log.error("Failed to process fleet alert message", e);
        }
    }
}
