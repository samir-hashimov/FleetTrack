package com.fleettrack.service;

import com.fleettrack.dao.entity.Vehicle;
import com.fleettrack.dao.repository.VehicleRepository;
import com.fleettrack.dto.request.VehicleRequest;
import com.fleettrack.dto.response.PageResponse;
import com.fleettrack.dto.response.VehicleResponse;
import com.fleettrack.dto.response.VehicleSummaryResponse;
import com.fleettrack.exception.BusinessException;
import com.fleettrack.exception.EntityNotFoundException;
import com.fleettrack.mapper.PageMapper;
import com.fleettrack.mapper.VehicleMapper;
import com.fleettrack.specification.VehicleSpecifications;
import com.fleettrack.util.VehicleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.fleettrack.config.CacheConfig.VEHICLE_SUMMARY_CACHE;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final PageMapper pageMapper;

    @CacheEvict(value = VEHICLE_SUMMARY_CACHE, allEntries = true)
    public VehicleResponse create(VehicleRequest request) {
        if (vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new BusinessException("Vehicle with license plate already exists: " + request.getLicensePlate());
        }
        Vehicle vehicle = vehicleMapper.toEntity(request);
        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional(readOnly = true)
    public VehicleResponse getById(Long id) {
        return vehicleMapper.toResponse(findVehicleOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<VehicleResponse> search(
            VehicleStatus status,
            Integer minYear,
            Integer maxYear,
            Long driverId,
            Boolean hasAssignedDriver,
            String licensePlate,
            Pageable pageable
    ) {
        Specification<Vehicle> spec = Specification.allOf(
                VehicleSpecifications.fetchDriver(),
                VehicleSpecifications.hasStatus(status),
                VehicleSpecifications.hasMinYear(minYear),
                VehicleSpecifications.hasMaxYear(maxYear),
                VehicleSpecifications.hasAssignedDriver(driverId),
                VehicleSpecifications.hasAssignedDriver(hasAssignedDriver),
                VehicleSpecifications.licensePlateContains(licensePlate)
        );

        Page<Vehicle> page = vehicleRepository.findAll(spec, pageable);
        return pageMapper.toPageResponse(page, vehicleMapper::toResponse);
    }

    @Cacheable(value = VEHICLE_SUMMARY_CACHE)
    @Transactional(readOnly = true)
    public List<VehicleSummaryResponse> getSummaries() {
        return vehicleRepository.findAll().stream()
                .map(vehicleMapper::toSummary)
                .toList();
    }

    @CacheEvict(value = VEHICLE_SUMMARY_CACHE, allEntries = true)
    public VehicleResponse update(Long id, VehicleRequest request) {
        Vehicle vehicle = findVehicleOrThrow(id);

        if (!vehicle.getLicensePlate().equals(request.getLicensePlate())
                && vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new BusinessException("Vehicle with license plate already exists: " + request.getLicensePlate());
        }

        vehicleMapper.updateEntity(request, vehicle);
        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @CacheEvict(value = VEHICLE_SUMMARY_CACHE, allEntries = true)
    public void delete(Long id) {
        Vehicle vehicle = findVehicleOrThrow(id);
        vehicle.setStatus(VehicleStatus.OFFLINE);
        vehicleRepository.save(vehicle);
    }

    Vehicle findVehicleOrThrow(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));
    }
}
