package com.example.gateway.dto;

public class AuthResponseDTO {
  private boolean success;
  private String message;
  private String email;
  private String role;
  private String token;

  // Constructor
  public AuthResponseDTO(boolean success, String message, String email, String role, String token) {
    this.success = success;
    this.message = message;
    this.email = email;
    this.role = role;
    this.token = token;
  }

  // Getters và Setters
  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
