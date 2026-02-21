package com.tradeops.mapper;

import com.tradeops.model.dto.UserDTO;
import com.tradeops.model.entity.UserEntity;
import com.tradeops.model.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "fullName", target = "fullName")
    UserResponse toUserResponse(UserEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "username", expression = "java(dto.getEmail())")
    UserEntity toEntity(UserDTO dto);
}
