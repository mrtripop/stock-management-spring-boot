package com.mrtripop.users.services;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.users.models.dto.CreateUserRequest;
import com.mrtripop.users.models.dto.UpdateUserRequest;
import com.mrtripop.users.models.dto.UserDto;
import org.springframework.data.domain.Page;

public interface UserService {

  Page<UserDto> retrieveUsers(BaseQueryParams queryParams) throws ApplicationException;

  UserDto retrieveUserById(Long id) throws ApplicationException;

  UserDto createNewUser(CreateUserRequest request) throws ApplicationException;

  UserDto updateUserGeneralInfo(Long id, UpdateUserRequest request) throws ApplicationException;

  void deleteUserById(Long id) throws ApplicationException;
}