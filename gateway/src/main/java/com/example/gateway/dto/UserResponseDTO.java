package com.example.gateway.dto;

import lombok.Data;

@Data
public class UserResponseDTO {
  private String name;
  private String email;
  private String password;
  private String role;
  private String userId; // Thêm userId
}
