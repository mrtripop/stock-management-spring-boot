package com.mrtripop.users.models.dto;

import com.mrtripop.users.models.UserRole;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
  private String accessToken;
  private String tokenType;
  private long expiresIn;
  private UserRole role;
  private String username;
  private UUID storeId;
}