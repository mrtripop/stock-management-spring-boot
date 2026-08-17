package com.mrtripop.users.component;

import com.mrtripop.users.models.User;
import com.mrtripop.users.models.dto.CreateUserRequest;
import com.mrtripop.users.models.dto.UpdateUserRequest;
import com.mrtripop.users.models.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

  UserDto toDto(User entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "passwordHash", ignore = true)
  @Mapping(target = "registeredAt", ignore = true)
  @Mapping(target = "lastLogin", ignore = true)
  User toEntity(CreateUserRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "passwordHash", ignore = true)
  @Mapping(target = "registeredAt", ignore = true)
  @Mapping(target = "lastLogin", ignore = true)
  void updateEntity(UpdateUserRequest request, @MappingTarget User entity);
}