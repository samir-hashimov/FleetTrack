package com.fleettrack.service;

import com.fleettrack.dao.entity.MaintenanceLog;
import com.fleettrack.dao.entity.Vehicle;
import com.fleettrack.dao.repository.MaintenanceLogRepository;
import com.fleettrack.dto.request.MaintenanceLogRequest;
import com.fleettrack.dto.response.MaintenanceLogResponse;
import com.fleettrack.dto.response.PageResponse;
import com.fleettrack.exception.EntityNotFoundException;
import com.fleettrack.mapper.MaintenanceLogMapper;
import com.fleettrack.mapper.PageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceLogService {

    private final MaintenanceLogRepository maintenanceLogRepository;
    private final VehicleService vehicleService;
    private final MaintenanceLogMapper maintenanceLogMapper;
    private final PageMapper pageMapper;

    public MaintenanceLogResponse create(MaintenanceLogRequest request) {
        Vehicle vehicle = vehicleService.findVehicleOrThrow(request.getVehicleId());
        MaintenanceLog log = maintenanceLogMapper.toEntity(request, vehicle);
        return maintenanceLogMapper.toResponse(maintenanceLogRepository.save(log));
    }

    @Transactional(readOnly = true)
    public MaintenanceLogResponse getById(Long id) {
        return maintenanceLogMapper.toResponse(findLogOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<MaintenanceLogResponse> getByVehicleId(Long vehicleId) {
        return maintenanceLogRepository.findByVehicleId(vehicleId).stream()
                .map(maintenanceLogMapper::toResponse)
                .toList();
    }

    // Internal use for PDF generation (No pagination)
    @Transactional(readOnly = true)
    public List<MaintenanceLogResponse> getAllAsList() {
        return maintenanceLogRepository.findAll().stream()
                .map(maintenanceLogMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<MaintenanceLogResponse> getAll(Pageable pageable) {
        Page<MaintenanceLog> page = maintenanceLogRepository.findAll(pageable);
        return pageMapper.toPageResponse(page, maintenanceLogMapper::toResponse);
    }

    public MaintenanceLogResponse update(Long id, MaintenanceLogRequest request) {
        MaintenanceLog log = findLogOrThrow(id);
        maintenanceLogMapper.updateEntity(request, log);

        if (request.getVehicleId() != null && !request.getVehicleId().equals(log.getVehicle().getId())) {
            Vehicle vehicle = vehicleService.findVehicleOrThrow(request.getVehicleId());
            log.setVehicle(vehicle);
        }
        return maintenanceLogMapper.toResponse(maintenanceLogRepository.save(log));
    }

    public void delete(Long id) {
        MaintenanceLog log = findLogOrThrow(id);
        maintenanceLogRepository.delete(log);
    }

    MaintenanceLog findLogOrThrow(Long id) {
        return maintenanceLogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Maintenance log not found with id: " + id));
    }
}