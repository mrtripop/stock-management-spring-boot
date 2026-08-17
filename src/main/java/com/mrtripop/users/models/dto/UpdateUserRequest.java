package com.mrtripop.users.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

  private Long roleId;
  private String firstName;
  private String lastName;
  private String username;
  private String mobile;
  private String email;
  private String intro;
  private String profile;
}