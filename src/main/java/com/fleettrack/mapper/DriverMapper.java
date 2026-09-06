package com.fleettrack.mapper;

import com.fleettrack.dao.entity.ContactData;
import com.fleettrack.dao.entity.Driver;
import com.fleettrack.dto.request.ContactDataRequest;
import com.fleettrack.dto.request.DriverRequest;
import com.fleettrack.dto.response.ContactDataResponse;
import com.fleettrack.dto.response.DriverResponse;
import com.fleettrack.dto.response.DriverSummaryResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "assignedVehicle", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Driver toEntity(DriverRequest request);

    @Mapping(target = "assignedVehicleId", source = "assignedVehicle.id")
    @Mapping(target = "assignedVehiclePlate", source = "assignedVehicle.licensePlate")
    @Mapping(target = "userId", source = "user.id")
    DriverResponse toResponse(Driver driver);

    @Mapping(target = "fullName", expression = "java(driver.getFirstName() + \" \" + driver.getLastName())")
    @Mapping(target = "assignedVehiclePlate", source = "assignedVehicle.licensePlate")
    DriverSummaryResponse toSummary(Driver driver);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "assignedVehicle", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(DriverRequest request, @MappingTarget Driver driver);

    ContactData toContactData(ContactDataRequest request);

    ContactDataResponse toContactDataResponse(ContactData contactData);
}