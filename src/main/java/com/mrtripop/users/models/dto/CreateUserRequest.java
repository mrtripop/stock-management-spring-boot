package com.mrtripop.users.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

  @NotBlank(message = "Username is required")
  @Size(max = 50, message = "Username must be at most 50 characters")
  private String username;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  @Size(max = 50, message = "Email must be at most 50 characters")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
  private String password;

  private Long roleId;

  @Size(max = 50, message = "First name must be at most 50 characters")
  private String firstName;

  @Size(max = 50, message = "Last name must be at most 50 characters")
  private String lastName;

  @Size(max = 15, message = "Mobile must be at most 15 characters")
  private String mobile;

  private String intro;
  private String profile;
}