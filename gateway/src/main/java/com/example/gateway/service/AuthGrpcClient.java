package com.example.gateway.service;

import auth.*;
import net.devh.boot.grpc.client.inject.GrpcClient;

import org.springframework.stereotype.Service;

@Service
public class AuthGrpcClient {

  @GrpcClient("authServiceChannel")
  private AuthServiceGrpc.AuthServiceBlockingStub authServiceStub;

  /**
   * Gửi yêu cầu đăng ký tới Auth-Service qua gRPC.
   *
   * @param email    Email của user.
   * @param password Mật khẩu của user.
   * @param role     Vai trò của user.
   * @return Phản hồi từ Auth-Service.
   */
  public AuthResponse register(String email, String password, String role) {
    RegisterRequest request = RegisterRequest.newBuilder()
        .setEmail(email)
        .setPassword(password)
        .setRole(role)
        .build();

    return authServiceStub.register(request);
  }

  /**
   * Gửi yêu cầu đăng nhập tới Auth-Service qua gRPC.
   *
   * @param email    Email của user.
   * @param password Mật khẩu của user.
   * @return Phản hồi từ Auth-Service.
   */
  public AuthResponse login(String email, String password) {
    LoginRequest request = LoginRequest.newBuilder()
        .setEmail(email)
        .setPassword(password)
        .build();

    return authServiceStub.login(request);
  }

  /**
   * Gửi yêu cầu xác minh token tới Auth-Service qua gRPC.
   *
   * @param token JWT token cần xác minh.
   * @return Phản hồi từ Auth-Service.
   */
  public VerifyTokenResponse verifyToken(String token) {
    VerifyTokenRequest request = VerifyTokenRequest.newBuilder()
        .setToken(token)
        .build();

    return authServiceStub.verifyToken(request);
  }
}
