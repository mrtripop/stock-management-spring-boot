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
  AUTH_USER_CREATED("USR2006", "User created successfully"),
  AUTH_MFA_REQUIRED("USR2007", "MFA verification required"),
  AUTH_TOTP_VERIFIED("USR2008", "TOTP verification successful"),
  // User CRUD operations
  USR2009_GET_ALL_USERS_SUCCESS("USR2009", "Users retrieved successfully"),
  USR2010_GET_USER_BY_ID_SUCCESS("USR2010", "User retrieved successfully"),
  USR2011_CREATE_USER_SUCCESS("USR2011", "User created successfully"),
  USR2012_UPDATE_USER_SUCCESS("USR2012", "User updated successfully"),
  USR2013_DELETE_USER_SUCCESS("USR2013", "User deleted successfully");

  private final String code;
  private final String message;
}