package com.example.auth_service.model;

public enum Role {
  ADMIN,
  USER,
  MODERATOR;

  public static boolean isValidRole(String role) {
    for (Role r : Role.values()) {
      if (r.name().equalsIgnoreCase(role)) {
        return true;
      }
    }
    return false;
  }
}
