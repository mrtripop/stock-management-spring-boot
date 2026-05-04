package com.mrtripop.users.constant;

import com.mrtripop.constant.BaseStatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements BaseStatusCode {
  AUTH_INVALID_CREDENTIALS("USR4001", "Invalid credentials"),
  AUTH_MFA_REQUIRED("USR4002", "MFA verification required"),
  AUTH_INVALID_MFA_CODE("USR4003", "Invalid MFA code"),
  AUTH_MFA_NOT_ENABLED("USR4004", "MFA is not enabled for this user"),
  AUTH_USER_NOT_FOUND("USR4005", "User not found"),
  AUTH_TOKEN_EXPIRED("USR4006", "Token has expired"),
  AUTH_TOKEN_INVALID("USR4007", "Invalid token"),
  AUTH_USERNAME_EXISTS("USR4008", "Username already exists"),
  AUTH_ACCESS_DENIED("USR4009", "Access denied");

  private final String code;
  private final String message;
}