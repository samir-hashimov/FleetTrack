package com.fleettrack.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleettrack.config.RedisConfig;
import com.fleettrack.dto.response.FleetAlertMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class FleetAlertPublisher {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publishMaintenanceDue(Long vehicleId, String licensePlate, String description) {
        FleetAlertMessage alert = FleetAlertMessage.builder()
                .type("MAINTENANCE_DUE")
                .message("Maintenance due for vehicle %s: %s".formatted(licensePlate, description))
                .vehicleId(vehicleId)
                .licensePlate(licensePlate)
                .timestamp(Instant.now())
                .build();
        publish(alert);
    }

    public void publishMaintenanceOverdue(Long vehicleId, String licensePlate, String description) {
        FleetAlertMessage alert = FleetAlertMessage.builder()
                .type("MAINTENANCE_OVERDUE")
                .message("Maintenance overdue for vehicle %s: %s".formatted(licensePlate, description))
                .vehicleId(vehicleId)
                .licensePlate(licensePlate)
                .timestamp(Instant.now())
                .build();
        publish(alert);
    }

    public void publishVehicleOffline(Long vehicleId, String licensePlate) {
        FleetAlertMessage alert = FleetAlertMessage.builder()
                .type("VEHICLE_OFFLINE")
                .message("Vehicle %s is offline".formatted(licensePlate))
                .vehicleId(vehicleId)
                .licensePlate(licensePlate)
                .timestamp(Instant.now())
                .build();
        publish(alert);
    }

    private void publish(FleetAlertMessage alert) {
        try {
            String payload = objectMapper.writeValueAsString(alert);
            redisTemplate.convertAndSend(RedisConfig.FLEET_ALERTS_CHANNEL, payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize fleet alert", e);
        }
    }
}
