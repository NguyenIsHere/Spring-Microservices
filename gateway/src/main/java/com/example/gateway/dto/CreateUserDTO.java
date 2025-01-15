package com.example.gateway.dto;

import lombok.Data;

@Data
public class CreateUserDTO {
  private String name;
  private String email;
  private String password;
}
