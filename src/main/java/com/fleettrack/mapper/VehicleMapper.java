package com.fleettrack.mapper;

import com.fleettrack.dto.request.VehicleRequest;
import com.fleettrack.dto.response.VehicleResponse;
import com.fleettrack.dto.response.VehicleSummaryResponse;
import com.fleettrack.dao.entity.Vehicle;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "latitude", ignore = true)
    @Mapping(target = "longitude", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Vehicle toEntity(VehicleRequest request);

    @Mapping(target = "assignedDriverId", source = "driver.id")
    @Mapping(target = "assignedDriverName", expression = "java(driverFullName(vehicle))")
    VehicleResponse toResponse(Vehicle vehicle);

    VehicleSummaryResponse toSummary(Vehicle vehicle);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "latitude", ignore = true)
    @Mapping(target = "longitude", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(VehicleRequest request, @MappingTarget Vehicle vehicle);

    default String driverFullName(Vehicle vehicle) {
        if (vehicle.getDriver() == null) {
            return null;
        }
        return vehicle.getDriver().getFirstName() + " " + vehicle.getDriver().getLastName();
    }
}
