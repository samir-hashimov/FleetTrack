package com.fleettrack.dao.repository;

import com.fleettrack.dao.entity.Vehicle;
import com.fleettrack.util.VehicleStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {

    @Override
    @EntityGraph(attributePaths = "driver")
    Optional<Vehicle> findById(Long id);

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    boolean existsByLicensePlate(String licensePlate);

    long countByStatus(VehicleStatus status);
}
