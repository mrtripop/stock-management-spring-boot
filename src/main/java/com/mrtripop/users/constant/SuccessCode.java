package com.mrtripop.users.constant;

import com.mrtripop.constant.BaseStatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements BaseStatusCode {
  AUTH_LOGIN_SUCCESS("USR2001", "Login successful"),
  AUTH_MFA_VERIFIED("USR2002", "MFA verification successful"),
  AUTH_STORE_SELECTED("USR2003", "Store selected successfully"),
  AUTH_MFA_SETUP("USR2004", "MFA setup completed"),
  AUTH_CURRENT_USER("USR2005", "Current user retrieved successfully"),
  AUTH_USER_CREATED("USR2006", "User created successfully");

  private final String code;
  private final String message;
}