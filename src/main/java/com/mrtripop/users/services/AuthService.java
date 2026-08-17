package com.mrtripop.users.services;

import com.mrtripop.exception.ApplicationException;
import com.mrtripop.users.models.dto.AuthResponse;
import com.mrtripop.users.models.dto.AuthUserDto;
import com.mrtripop.users.models.dto.CreateAuthUserRequest;
import com.mrtripop.users.models.dto.LoginRequest;
import com.mrtripop.users.models.dto.LoginResponse;
import com.mrtripop.users.models.dto.MfaSetupResponse;
import com.mrtripop.users.models.dto.MfaVerifyRequest;
import com.mrtripop.users.models.dto.VerifyTotpResponse;
import java.util.UUID;

public interface AuthService {
  LoginResponse login(LoginRequest request) throws ApplicationException;
  AuthResponse verifyMfa(MfaVerifyRequest request) throws ApplicationException;
  VerifyTotpResponse verifyTotp(String tempToken, String totpCode) throws ApplicationException;
  AuthResponse selectStore(UUID userId, UUID storeId) throws ApplicationException;
  MfaSetupResponse setupMfa(UUID userId) throws ApplicationException;
  AuthUserDto getCurrentUser(UUID userId) throws ApplicationException;
  AuthUserDto register(CreateAuthUserRequest request) throws ApplicationException;
}