package com.mrtripop.users.services.impl;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.users.component.AuthUserMapper;
import com.mrtripop.users.component.JwtService;
import com.mrtripop.users.component.TotpService;
import com.mrtripop.users.constant.ErrorCode;
import com.mrtripop.users.constant.SuccessCode;
import com.mrtripop.users.models.UserRole;
import com.mrtripop.users.models.db.AuthUser;
import com.mrtripop.users.models.dto.AuthResponse;
import com.mrtripop.users.models.dto.AuthUserDto;
import com.mrtripop.users.models.dto.CreateAuthUserRequest;
import com.mrtripop.users.models.dto.LoginRequest;
import com.mrtripop.users.models.dto.LoginResponse;
import com.mrtripop.users.models.dto.MfaSetupResponse;
import com.mrtripop.users.models.dto.MfaVerifyRequest;
import com.mrtripop.users.models.dto.VerifyTotpResponse;
import com.mrtripop.users.repositories.AuthUserRepository;
import com.mrtripop.users.services.AuthService;
import com.mrtripop.clinical.repository.StoreRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final AuthUserRepository authUserRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final TotpService totpService;
  private final AuthUserMapper authUserMapper;
  private final StoreRepository storeRepository;

  @Override
  @Transactional(readOnly = true)
  public LoginResponse login(LoginRequest request) throws ApplicationException {
    AuthUser user = authUserRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new ApplicationException(ErrorCode.AUTH_INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new ApplicationException(ErrorCode.AUTH_INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
    }

    if (user.isMfaEnabled()) {
      String tempToken = jwtService.generateTempToken(user.getId());
      return LoginResponse.builder()
          .mfaRequired(true)
          .tempToken(tempToken)
          .message(SuccessCode.AUTH_MFA_REQUIRED.getMessage())
          .build();
    }

    String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRole(), null);
    return LoginResponse.builder()
        .mfaRequired(false)
        .accessToken(accessToken)
        .role(user.getRole())
        .message(SuccessCode.AUTH_LOGIN_SUCCESS.getMessage())
        .build();
  }

  private static final String TOKEN_TYPE_TEMP = "TEMP";
  private static final String TOKEN_TYPE_BEARER = "Bearer";

  @Override
  @Transactional(readOnly = true)
  public AuthResponse verifyMfa(MfaVerifyRequest request) throws ApplicationException {
    io.jsonwebtoken.Claims claims = jwtService.extractClaims(request.getTempToken());

    if (!TOKEN_TYPE_TEMP.equals(claims.get("type", String.class))) {
      throw new ApplicationException(ErrorCode.AUTH_TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
    }

    UUID userId = UUID.fromString(claims.getSubject());
    AuthUser user = authUserRepository.findById(userId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.AUTH_USER_NOT_FOUND, HttpStatus.UNAUTHORIZED));

    if (!totpService.validateCode(user.getMfaSecret(), request.getTotpCode())) {
      throw new ApplicationException(ErrorCode.AUTH_INVALID_MFA_CODE, HttpStatus.UNAUTHORIZED);
    }

    String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRole(), null);
    return AuthResponse.builder()
        .accessToken(accessToken)
        .tokenType(TOKEN_TYPE_BEARER)
        .expiresIn(jwtService.getAccessTokenExpiration())
        .role(user.getRole())
        .username(user.getUsername())
        .storeId(null)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public VerifyTotpResponse verifyTotp(String tempToken, String totpCode) throws ApplicationException {
    io.jsonwebtoken.Claims claims = jwtService.extractClaims(tempToken);

    UUID userId = UUID.fromString(claims.getSubject());
    AuthUser user = authUserRepository.findById(userId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.AUTH_USER_NOT_FOUND, HttpStatus.UNAUTHORIZED));

    if (!totpService.validateCode(user.getMfaSecret(), totpCode)) {
      throw new ApplicationException(ErrorCode.AUTH_INVALID_MFA_CODE, HttpStatus.UNAUTHORIZED);
    }

    String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRole(), null);
    return VerifyTotpResponse.builder()
        .token(accessToken)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public AuthResponse selectStore(UUID userId, UUID storeId) throws ApplicationException {
    AuthUser user = authUserRepository.findById(userId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.AUTH_USER_NOT_FOUND, HttpStatus.NOT_FOUND));

    if (!storeRepository.existsById(storeId)) {
      throw new ApplicationException(ErrorCode.STORE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRole(), storeId);
    return AuthResponse.builder()
        .accessToken(accessToken)
        .tokenType("Bearer")
        .expiresIn(jwtService.getAccessTokenExpiration())
        .role(user.getRole())
        .username(user.getUsername())
        .storeId(storeId)
        .build();
  }

  @Override
  @Transactional(rollbackFor = ApplicationException.class)
  public MfaSetupResponse setupMfa(UUID userId) throws ApplicationException {
    AuthUser user = authUserRepository.findById(userId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.AUTH_USER_NOT_FOUND, HttpStatus.NOT_FOUND));

    String secret = totpService.generateSecret();
    String qrCodeDataUri = totpService.getQrCodeDataUri(user.getUsername(), secret);

    user.setMfaSecret(secret);
    user.setMfaEnabled(true);
    try {
      authUserRepository.save(user);
    } catch (ObjectOptimisticLockingFailureException e) {
      throw new ApplicationException(ErrorCode.CONCURRENT_MODIFICATION, HttpStatus.CONFLICT);
    }

    return MfaSetupResponse.builder()
        .secret(secret)
        .qrCodeDataUri(qrCodeDataUri)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public AuthUserDto getCurrentUser(UUID userId) throws ApplicationException {
    AuthUser user = authUserRepository.findById(userId)
        .orElseThrow(() -> new ApplicationException(ErrorCode.AUTH_USER_NOT_FOUND, HttpStatus.NOT_FOUND));
    return authUserMapper.toDto(user);
  }

  @Override
  @Transactional(rollbackFor = ApplicationException.class)
  public AuthUserDto register(CreateAuthUserRequest request) throws ApplicationException {
    if (authUserRepository.existsByUsername(request.getUsername())) {
      throw new ApplicationException(ErrorCode.AUTH_USERNAME_EXISTS, HttpStatus.CONFLICT);
    }

    AuthUser user = AuthUser.builder()
        .username(request.getUsername())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(UserRole.EMPLOYEE)
        .mfaEnabled(false)
        .build();

    try {
      AuthUser saved = authUserRepository.save(user);
      return authUserMapper.toDto(saved);
    } catch (DataIntegrityViolationException e) {
      throw new ApplicationException(ErrorCode.AUTH_USERNAME_EXISTS, HttpStatus.CONFLICT);
    }
  }
}