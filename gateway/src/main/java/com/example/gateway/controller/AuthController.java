package com.example.gateway.controller;

import com.example.gateway.service.AuthGrpcClient;

import auth.AuthResponse;
import auth.VerifyTokenResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthGrpcClient authGrpcClient;

  public AuthController(AuthGrpcClient authGrpcClient) {
    this.authGrpcClient = authGrpcClient;
  }

  /**
   * API để đăng ký người dùng mới.
   *
   * @param email    Email của user.
   * @param password Mật khẩu của user.
   * @param role     Vai trò của user.
   * @return Phản hồi từ Auth-Service.
   */
  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestParam String email,
      @RequestParam String password,
      @RequestParam String role) {
    AuthResponse response = authGrpcClient.register(email, password, role);

    if (response.getSuccess()) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getMessage());
    }
  }

  /**
   * API để đăng nhập.
   *
   * @param email    Email của user.
   * @param password Mật khẩu của user.
   * @return Phản hồi từ Auth-Service.
   */
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestParam String email,
      @RequestParam String password) {
    AuthResponse response = authGrpcClient.login(email, password);

    if (response.getSuccess()) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response.getMessage());
    }
  }

  /**
   * API để xác minh token JWT.
   *
   * @param token JWT token.
   * @return Phản hồi từ Auth-Service.
   */
  @GetMapping("/verify-token")
  public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String token) {
    try {
      // Loại bỏ tiền tố "Bearer " nếu có trong token
      if (token.startsWith("Bearer ")) {
        token = token.substring(7);
      }

      VerifyTokenResponse response = authGrpcClient.verifyToken(token);

      if (response.getIsValid()) {
        return ResponseEntity.ok(response);
      } else {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error verifying token: " + e.getMessage());
    }
  }
}
