package com.mrtripop.users.services.impl;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.users.component.UserMapper;
import com.mrtripop.users.constant.ErrorCode;
import com.mrtripop.users.models.User;
import com.mrtripop.users.models.dto.CreateUserRequest;
import com.mrtripop.users.models.dto.UpdateUserRequest;
import com.mrtripop.users.models.dto.UserDto;
import com.mrtripop.users.repositories.UserRepository;
import com.mrtripop.users.services.UserService;
import com.mrtripop.util.DatabaseHelper;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  public Page<UserDto> retrieveUsers(BaseQueryParams queryParams) throws ApplicationException {
    Pageable pageable = DatabaseHelper.initPageableWithSort(queryParams);
    Page<User> users = userRepository.findAll(pageable);
    return users.map(userMapper::toDto);
  }

  @Override
  public UserDto retrieveUserById(Long id) throws ApplicationException {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ApplicationException(ErrorCode.AUTH_USER_NOT_FOUND, HttpStatus.NOT_FOUND));
    return userMapper.toDto(user);
  }

  @Override
  public UserDto createNewUser(CreateUserRequest request) throws ApplicationException {
    if (userRepository.findByUsername(request.getUsername()).isPresent()) {
      throw new ApplicationException(ErrorCode.AUTH_USERNAME_EXISTS, HttpStatus.CONFLICT);
    }
    User user = userMapper.toEntity(request);
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setRegisteredAt(ZonedDateTime.now());
    user.setLastLogin(ZonedDateTime.now());
    User saved = userRepository.save(user);
    return userMapper.toDto(saved);
  }

  @Override
  public UserDto updateUserGeneralInfo(Long id, UpdateUserRequest request) throws ApplicationException {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ApplicationException(ErrorCode.AUTH_USER_NOT_FOUND, HttpStatus.NOT_FOUND));
    userMapper.updateEntity(request, user);
    User saved = userRepository.save(user);
    return userMapper.toDto(saved);
  }

  @Override
  public void deleteUserById(Long id) throws ApplicationException {
    if (!userRepository.existsById(id)) {
      throw new ApplicationException(ErrorCode.AUTH_USER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
    userRepository.deleteById(id);
  }
}