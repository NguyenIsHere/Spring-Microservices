package com.example.gateway.dto;

public class VerifyTokenResponseDTO {
  private boolean isValid;
  private String email;
  private String role;
  private String userId;

  // Constructor
  public VerifyTokenResponseDTO(boolean isValid, String email, String role, String userId) {
    this.isValid = isValid;
    this.email = email;
    this.role = role;
    this.userId = userId;
  }

  // Getters và Setters
  public boolean isValid() {
    return isValid;
  }

  public void setValid(boolean valid) {
    isValid = valid;
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

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}
