package com.mrtripop.users.models.dto;

import com.mrtripop.users.models.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
  private boolean mfaRequired;
  private String tempToken;
  private String accessToken;
  private UserRole role;
  private String message;
}