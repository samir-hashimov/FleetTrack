package com.fleettrack.service;

import com.fleettrack.dao.repository.DriverRepository;
import com.fleettrack.dto.request.VehicleLocationRequest;
import com.fleettrack.dto.response.VehicleLocationResponse;
import com.fleettrack.dao.entity.Vehicle;
import com.fleettrack.exception.BusinessException;
import com.fleettrack.util.VehicleStatus;
import com.fleettrack.redis.FleetAlertPublisher;
import com.fleettrack.dao.repository.VehicleRepository;
import com.fleettrack.websocket.VehicleLocationBroadcaster;
import com.fleettrack.exception.UnauthorizedActionException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleLocationService {

    private final VehicleRepository vehicleRepository;
    private final VehicleService vehicleService;
    private final VehicleLocationBroadcaster locationBroadcaster;
    private final FleetAlertPublisher fleetAlertPublisher;
    private final DriverRepository driverRepository ;

    public VehicleLocationResponse updateLocation(VehicleLocationRequest request) {
        Vehicle vehicle = vehicleService.findVehicleOrThrow(request.getVehicleId());
        boolean hasDriver = driverRepository.existsByAssignedVehicleId(request.getVehicleId());
        if (!hasDriver) {
            throw new BusinessException("Cannot update location. This vehicle does not have an assigned driver.");
        }
        verifyVehicleAccess(vehicle);

        vehicle.setLatitude(request.getLatitude());
        vehicle.setLongitude(request.getLongitude());

        if (vehicle.getStatus() == VehicleStatus.OFFLINE) {
            vehicle.setStatus(VehicleStatus.ACTIVE);
        }

        vehicleRepository.save(vehicle);

        VehicleLocationResponse response = VehicleLocationResponse.builder()
                .vehicleId(vehicle.getId())
                .licensePlate(vehicle.getLicensePlate())
                .latitude(vehicle.getLatitude())
                .longitude(vehicle.getLongitude())
                .timestamp(Instant.now())
                .build();

        locationBroadcaster.broadcast(response);
        return response;
    }

    public void markVehicleOffline(Long vehicleId) {
        Vehicle vehicle = vehicleService.findVehicleOrThrow(vehicleId);

        verifyVehicleAccess(vehicle);

        vehicle.setStatus(VehicleStatus.OFFLINE);
        vehicleRepository.save(vehicle);

        fleetAlertPublisher.publishVehicleOffline(vehicle.getId(), vehicle.getLicensePlate());
    }

    private void verifyVehicleAccess(Vehicle vehicle) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        boolean isAdminOrManager = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_FLEET_MANAGER"));

        if (!isAdminOrManager) {
            if (vehicle.getDriver() == null || vehicle.getDriver().getUser() == null ||
                    !vehicle.getDriver().getUser().getUsername().equals(username)) {
                throw new UnauthorizedActionException("You are only allowed to update the location of your assigned vehicle.");
            }
        }
    }
}