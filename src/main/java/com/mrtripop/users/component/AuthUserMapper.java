package com.mrtripop.users.component;

import com.mrtripop.users.models.db.AuthUser;
import com.mrtripop.users.models.dto.AuthUserDto;
import com.mrtripop.users.models.dto.CreateAuthUserRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthUserMapper {
  AuthUserDto toDto(AuthUser entity);

  AuthUser toEntity(CreateAuthUserRequest request);
}