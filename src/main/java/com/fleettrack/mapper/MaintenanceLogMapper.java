package com.fleettrack.mapper;

import com.fleettrack.dao.entity.MaintenanceLog;
import com.fleettrack.dao.entity.Vehicle;
import com.fleettrack.dto.request.MaintenanceLogRequest;
import com.fleettrack.dto.response.MaintenanceLogResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MaintenanceLogMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "vehicle", source = "vehicle")
    @Mapping(target = "completed", source = "request.completed")
    MaintenanceLog toEntity(MaintenanceLogRequest request, Vehicle vehicle);

    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleLicensePlate", source = "vehicle.licensePlate")
    MaintenanceLogResponse toResponse(MaintenanceLog maintenanceLog);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(MaintenanceLogRequest request, @MappingTarget MaintenanceLog maintenanceLog);
}
