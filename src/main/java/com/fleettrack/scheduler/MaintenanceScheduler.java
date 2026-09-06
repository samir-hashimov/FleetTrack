package com.fleettrack.scheduler;

import com.fleettrack.dao.entity.MaintenanceLog;
import com.fleettrack.redis.FleetAlertPublisher;
import com.fleettrack.dao.repository.MaintenanceLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class MaintenanceScheduler {

    private final MaintenanceLogRepository maintenanceLogRepository;
    private final FleetAlertPublisher fleetAlertPublisher;

    @Value("${fleettrack.scheduler.maintenance-alert-days-ahead:7}")
    private int daysAhead;

    @Scheduled(cron = "${fleettrack.scheduler.maintenance-check-cron:0 0 6 * * *}")
    @Transactional(readOnly = true)
    public void checkMaintenanceAlerts() {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(daysAhead);

        List<MaintenanceLog> logs = maintenanceLogRepository.findUpcomingOrOverdue(threshold);
        log.info("Maintenance scheduler found {} upcoming/overdue logs", logs.size());

        for (MaintenanceLog logEntry : logs) {
            String plate = logEntry.getVehicle().getLicensePlate();
            if (logEntry.getScheduledDate().isBefore(today)) {
                fleetAlertPublisher.publishMaintenanceOverdue(
                        logEntry.getVehicle().getId(), plate, logEntry.getDescription());
            } else {
                fleetAlertPublisher.publishMaintenanceDue(
                        logEntry.getVehicle().getId(), plate, logEntry.getDescription());
            }
        }
    }
}
