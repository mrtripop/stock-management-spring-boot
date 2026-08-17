package com.mrtripop.users.models.dto;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
  private Long id;
  private Long roleId;
  private String firstName;
  private String lastName;
  private String username;
  private String mobile;
  private String email;
  private String intro;
  private String profile;
  private ZonedDateTime registeredAt;
  private ZonedDateTime lastLogin;
}