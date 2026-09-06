package com.fleettrack.dao.repository;

import com.fleettrack.dao.entity.MaintenanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Long> {

    List<MaintenanceLog> findByVehicleId(Long vehicleId);

    @Query("""
            SELECT ml FROM MaintenanceLog ml
            JOIN FETCH ml.vehicle v
            WHERE ml.completed = false
              AND ml.scheduledDate <= :thresholdDate
            ORDER BY ml.scheduledDate ASC
            """)
    List<MaintenanceLog> findUpcomingOrOverdue(@Param("thresholdDate") LocalDate thresholdDate);
}
