package com.example.graphql_gateway.dto;

import lombok.Data;

@Data
public class UserDTO {
  private String name;
  private String email;
  private String userId;
  private String role;
}
