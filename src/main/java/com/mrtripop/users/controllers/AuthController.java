package com.mrtripop.users.controllers;

import com.mrtripop.constant.BaseStatusCode;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.model.ResponseBody;
import com.mrtripop.users.constant.SuccessCode;
import com.mrtripop.users.models.dto.AuthResponse;
import com.mrtripop.users.models.dto.AuthUserDto;
import com.mrtripop.users.models.dto.CreateAuthUserRequest;
import com.mrtripop.users.models.dto.LoginRequest;
import com.mrtripop.users.models.dto.LoginResponse;
import com.mrtripop.users.models.dto.MfaSetupResponse;
import com.mrtripop.users.models.dto.MfaVerifyRequest;
import com.mrtripop.users.models.dto.StoreSelectionRequest;
import com.mrtripop.users.services.AuthService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {
  private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest request) throws ApplicationException {
    LoginResponse result = authService.login(request);
    BaseStatusCode success = SuccessCode.AUTH_LOGIN_SUCCESS;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @PostMapping("/verify-mfa")
  public ResponseEntity<Object> verifyMfa(@Valid @RequestBody MfaVerifyRequest request) throws ApplicationException {
    AuthResponse result = authService.verifyMfa(request);
    BaseStatusCode success = SuccessCode.AUTH_MFA_VERIFIED;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @PostMapping("/select-store")
  public ResponseEntity<Object> selectStore(@Valid @RequestBody StoreSelectionRequest request, Principal principal)
      throws ApplicationException {
    UUID userId = UUID.fromString(principal.getName());
    AuthResponse result = authService.selectStore(userId, request.getStoreId());
    BaseStatusCode success = SuccessCode.AUTH_STORE_SELECTED;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @PostMapping("/setup-mfa")
  public ResponseEntity<Object> setupMfa(Principal principal) throws ApplicationException {
    UUID userId = UUID.fromString(principal.getName());
    MfaSetupResponse result = authService.setupMfa(userId);
    BaseStatusCode success = SuccessCode.AUTH_MFA_SETUP;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/me")
  public ResponseEntity<Object> getCurrentUser(Principal principal) throws ApplicationException {
    UUID userId = UUID.fromString(principal.getName());
    AuthUserDto result = authService.getCurrentUser(userId);
    BaseStatusCode success = SuccessCode.AUTH_CURRENT_USER;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.OK);
  }

  @PostMapping("/register")
  public ResponseEntity<Object> register(@Valid @RequestBody CreateAuthUserRequest request) throws ApplicationException {
    AuthUserDto result = authService.register(request);
    BaseStatusCode success = SuccessCode.AUTH_USER_CREATED;
    return ResponseBody.builder()
        .code(success.getCode())
        .message(success.getMessage())
        .data(result)
        .build()
        .toResponseEntity(HttpStatus.CREATED);
  }
}