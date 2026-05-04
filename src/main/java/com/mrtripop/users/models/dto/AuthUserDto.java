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
public class AuthUserDto {
  private UUID id;
  private String username;
  private UserRole role;
  private boolean mfaEnabled;
  private Long createdAt;
  private Long updatedAt;
}