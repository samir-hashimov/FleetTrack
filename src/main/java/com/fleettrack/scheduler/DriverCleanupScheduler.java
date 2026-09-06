package com.fleettrack.scheduler;

import com.fleettrack.dao.repository.DriverRepository;
import com.fleettrack.util.DriverStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriverCleanupScheduler {

    private final DriverRepository driverRepository;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void hardDeleteBlockedDrivers() {
        Instant thresholdDate = Instant.now().minus(30, ChronoUnit.DAYS);

        log.info("Starting hard deletion of BLOCKED drivers updated before: {}", thresholdDate);

        driverRepository.deleteByStatusAndUpdatedAtBefore(DriverStatus.BLOCKED, thresholdDate);

        log.info("Completed hard deletion of stale BLOCKED drivers.");
    }
}