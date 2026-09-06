package com.fleettrack.service;

import com.fleettrack.dao.entity.Driver;
import com.fleettrack.dao.entity.User;
import com.fleettrack.dao.entity.Vehicle;
import com.fleettrack.dao.repository.DriverRepository;
import com.fleettrack.dao.repository.UserRepository;
import com.fleettrack.dao.repository.VehicleRepository;
import com.fleettrack.dto.request.DriverRequest;
import com.fleettrack.dto.response.DriverResponse;
import com.fleettrack.dto.response.DriverSummaryResponse;
import com.fleettrack.dto.response.PageResponse;
import com.fleettrack.exception.BusinessException;
import com.fleettrack.exception.EntityNotFoundException;
import com.fleettrack.exception.UnauthorizedActionException;
import com.fleettrack.mapper.DriverMapper;
import com.fleettrack.mapper.PageMapper;
import com.fleettrack.util.DriverStatus;
import com.fleettrack.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.fleettrack.config.CacheConfig.DRIVER_SUMMARY_CACHE;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final DriverMapper driverMapper;
    private final PageMapper pageMapper;

    @CacheEvict(value = DRIVER_SUMMARY_CACHE, allEntries = true)
    public DriverResponse create(DriverRequest request) {
        if (driverRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new BusinessException("Driver with license number already exists: " + request.getLicenseNumber());
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.getUserId()));

        if (user.getRole() != Role.DRIVER) {
            throw new BusinessException("Driver profile can only be linked to a user with DRIVER role.");
        }

        if (driverRepository.existsByUserId(user.getId())) {
            throw new BusinessException("This user account is already linked to another driver profile.");
        }

        String requestedEmail = request.getContactData().getEmail().trim();
        if (!user.getEmail().equalsIgnoreCase(requestedEmail)) {
            throw new BusinessException("The provided email (" + requestedEmail + ") does not match the account email (" + user.getEmail() + ").");
        }

        Driver driver = driverMapper.toEntity(request);
        driver.setStatus(DriverStatus.ACTIVE);
        driver.setUser(user);
        driver.setAssignedVehicle(resolveVehicle(request.getAssignedVehicleId(), null));

        return driverMapper.toResponse(driverRepository.save(driver));
    }

    @Transactional(readOnly = true)
    public DriverResponse getById(Long id) {
        verifyDriverAccess(id);
        return driverMapper.toResponse(findDriverOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<DriverResponse> getAll(Pageable pageable) {
        Page<Driver> page = driverRepository.findAll(pageable);
        return pageMapper.toPageResponse(page, driverMapper::toResponse);
    }

    @Cacheable(value = DRIVER_SUMMARY_CACHE)
    @Transactional(readOnly = true)
    public List<DriverSummaryResponse> getSummaries() {
        return driverRepository.findAll().stream()
                .filter(d -> d.getStatus() == DriverStatus.ACTIVE)
                .map(driverMapper::toSummary)
                .toList();
    }

    @CacheEvict(value = DRIVER_SUMMARY_CACHE, allEntries = true)
    public DriverResponse update(Long id, DriverRequest request) {
        verifyDriverAccess(id);

        Driver driver = findDriverOrThrow(id);

        if (driver.getStatus() == DriverStatus.BLOCKED) {
            throw new UnauthorizedActionException("Blocked drivers cannot be updated.");
        }

        if (!driver.getLicenseNumber().equals(request.getLicenseNumber())
                && driverRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new BusinessException("Driver with license number already exists: " + request.getLicenseNumber());
        }

        driverMapper.updateEntity(request, driver);
        driver.setAssignedVehicle(resolveVehicle(request.getAssignedVehicleId(), driver.getId()));

        return driverMapper.toResponse(driverRepository.save(driver));
    }

    @CacheEvict(value = DRIVER_SUMMARY_CACHE, allEntries = true)
    public void delete(Long id) {
        Driver driver = findDriverOrThrow(id);
        driver.setStatus(DriverStatus.BLOCKED);
        driver.setAssignedVehicle(null);
        driverRepository.save(driver);
    }

    private void verifyDriverAccess(Long targetDriverId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        boolean isAdminOrManager = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_FLEET_MANAGER"));

        if (!isAdminOrManager) {
            Driver myDriver = driverRepository.findByUserEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException("Driver profile not found for email: " + email));

            if (!myDriver.getId().equals(targetDriverId)) {
                throw new UnauthorizedActionException("You are only allowed to access or update your own profile.");
            }
        }
    }

    private Vehicle resolveVehicle(Long vehicleId, Long currentDriverId) {
        if (vehicleId == null) return null;

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + vehicleId));

        driverRepository.findByAssignedVehicleId(vehicleId).ifPresent(existing -> {
            if (currentDriverId == null || !existing.getId().equals(currentDriverId)) {
                throw new BusinessException("Vehicle is already assigned to another driver");
            }
        });
        return vehicle;
    }

    private Driver findDriverOrThrow(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found with id: " + id));
    }
}