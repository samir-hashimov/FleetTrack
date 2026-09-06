package com.fleettrack.mapper;

import com.fleettrack.dao.entity.User;
import com.fleettrack.dto.response.AuthResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "refreshToken", source = "refreshToken")
//    @Mapping(target = "expiresIn", source = "expiresIn")
    AuthResponse toResponse(User user, String accessToken, String refreshToken);
}