package com.mrtripop.users.controllers;

import com.mrtripop.model.BaseQueryParams;
import com.mrtripop.model.ResponseBody;
import com.mrtripop.users.constant.SuccessCode;
import com.mrtripop.users.models.dto.CreateUserRequest;
import com.mrtripop.users.models.dto.UpdateUserRequest;
import com.mrtripop.users.models.dto.UserDto;
import com.mrtripop.users.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

  private final UserService userService;

  @GetMapping
  public ResponseEntity<Object> retrieveUsers(@Valid BaseQueryParams queryParams) {
    Page<UserDto> result = userService.retrieveUsers(queryParams);
    return ResponseBody.builder()
        .code(SuccessCode.USR2009_GET_ALL_USERS_SUCCESS.getCode())
        .message(SuccessCode.USR2009_GET_ALL_USERS_SUCCESS.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Object> retrieveUserById(@PathVariable Long id) {
    UserDto result = userService.retrieveUserById(id);
    return ResponseBody.builder()
        .code(SuccessCode.USR2010_GET_USER_BY_ID_SUCCESS.getCode())
        .message(SuccessCode.USR2010_GET_USER_BY_ID_SUCCESS.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<Object> createNewUser(@Valid @RequestBody CreateUserRequest request) {
    UserDto result = userService.createNewUser(request);
    return ResponseBody.builder()
        .code(SuccessCode.USR2011_CREATE_USER_SUCCESS.getCode())
        .message(SuccessCode.USR2011_CREATE_USER_SUCCESS.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.CREATED);
  }

  @PutMapping("/{id}/general")
  public ResponseEntity<Object> updateUserGeneralInfo(
      @PathVariable Long id, @RequestBody UpdateUserRequest request) {
    UserDto result = userService.updateUserGeneralInfo(id, request);
    return ResponseBody.builder()
        .code(SuccessCode.USR2012_UPDATE_USER_SUCCESS.getCode())
        .message(SuccessCode.USR2012_UPDATE_USER_SUCCESS.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Object> deleteUserById(@PathVariable Long id) {
    userService.deleteUserById(id);
    return ResponseBody.builder()
        .code(SuccessCode.USR2013_DELETE_USER_SUCCESS.getCode())
        .message(SuccessCode.USR2013_DELETE_USER_SUCCESS.getMessage())
        .build()
        .toResponseEntity(HttpStatus.OK);
  }
}