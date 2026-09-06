package com.fleettrack.dao.repository;

import com.fleettrack.dao.entity.Driver;
import com.fleettrack.util.DriverStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long>, JpaSpecificationExecutor<Driver> {

    @Override
    @EntityGraph(attributePaths = "assignedVehicle")
    Optional<Driver> findById(Long id);

    boolean existsByUserId(Long userId);

    Optional<Driver> findByLicenseNumber(String licenseNumber);

    boolean existsByLicenseNumber(String licenseNumber);

    Optional<Driver> findByAssignedVehicleId(Long vehicleId);

    Optional<Driver> findByUserEmail(String email);

    void deleteByStatusAndUpdatedAtBefore(DriverStatus status, Instant thresholdDate);

    boolean existsByAssignedVehicleId(Long vehicleId);
}
