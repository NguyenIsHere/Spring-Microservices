package com.example.gateway.controller;

import com.example.gateway.dto.AuthResponseDTO;
import com.example.gateway.dto.LoginRequestDTO;
import com.example.gateway.dto.RegisterRequestDTO;
import com.example.gateway.dto.VerifyTokenResponseDTO;
import com.example.gateway.service.AuthGrpcClient;

import auth.AuthResponse;
import auth.VerifyTokenResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthGrpcClient authGrpcClient;

  public AuthController(AuthGrpcClient authGrpcClient) {
    this.authGrpcClient = authGrpcClient;
  }

  /**
   * API để đăng ký người dùng mới.
   *
   * @param registerRequestDTO DTO chứa thông tin đăng ký.
   * @return Phản hồi từ Auth-Service.
   */
  @PostMapping("/register")
  public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
    // Gửi yêu cầu gRPC
    AuthResponse response = authGrpcClient.register(
        registerRequestDTO.getEmail(),
        registerRequestDTO.getPassword(),
        registerRequestDTO.getRole());

    // Tạo DTO phản hồi
    AuthResponseDTO responseDTO = new AuthResponseDTO(
        response.getSuccess(),
        response.getMessage(),
        response.getEmail(),
        response.getRole(),
        response.getToken());

    if (response.getSuccess()) {
      return ResponseEntity.ok(responseDTO);
    } else {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDTO);
    }
  }

  /**
   * API để đăng nhập.
   *
   * @param loginRequestDTO DTO chứa thông tin đăng nhập.
   * @return Phản hồi từ Auth-Service.
   */
  @PostMapping("/login")
  public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
    // Gửi yêu cầu gRPC
    AuthResponse response = authGrpcClient.login(
        loginRequestDTO.getEmail(),
        loginRequestDTO.getPassword());

    // Tạo DTO phản hồi
    AuthResponseDTO responseDTO = new AuthResponseDTO(
        response.getSuccess(),
        response.getMessage(),
        response.getEmail(),
        response.getRole(),
        response.getToken());

    if (response.getSuccess()) {
      return ResponseEntity.ok(responseDTO);
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDTO);
    }
  }

  /**
   * API để xác minh token JWT.
   *
   * @param token JWT token từ header Authorization.
   * @return Phản hồi từ Auth-Service.
   */
  @GetMapping("/verify-token")
  public ResponseEntity<VerifyTokenResponseDTO> verifyToken(@RequestHeader("Authorization") String token) {
    try {
      // Loại bỏ tiền tố "Bearer " nếu có trong token
      if (token.startsWith("Bearer ")) {
        token = token.substring(7);
      }

      // Gọi AuthGrpcClient để xác minh token
      VerifyTokenResponse response = authGrpcClient.verifyToken(token);

      if (response.getIsValid()) {
        // Tạo DTO phản hồi
        VerifyTokenResponseDTO responseDTO = new VerifyTokenResponseDTO(
            response.getIsValid(),
            response.getEmail(),
            response.getRole(),
            response.getUserId());
        return ResponseEntity.ok(responseDTO);
      } else {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new VerifyTokenResponseDTO(false, null, null, null));
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new VerifyTokenResponseDTO(false, null, null, null));
    }
  }

}
