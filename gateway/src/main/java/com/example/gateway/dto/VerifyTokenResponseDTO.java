package com.example.gateway.dto;

public class VerifyTokenResponseDTO {
  private boolean isValid;
  private String email;
  private String role;

  // Constructor
  public VerifyTokenResponseDTO(boolean isValid, String email, String role) {
    this.isValid = isValid;
    this.email = email;
    this.role = role;
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
}
