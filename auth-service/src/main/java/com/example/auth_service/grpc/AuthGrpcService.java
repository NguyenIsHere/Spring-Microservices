package com.example.auth_service.grpc;

import com.example.auth_service.util.JwtProvider;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import auth.*;
import user.*;

@GrpcService
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

  @GrpcClient("userServiceChannel")
  private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;

  public AuthGrpcService(PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
    this.passwordEncoder = passwordEncoder;
    this.jwtProvider = jwtProvider;
  }

  @Override
  public void register(RegisterRequest request, StreamObserver<AuthResponse> responseObserver) {
    try {
      UserResponse existingUser = userServiceStub.getUserByEmail(
          UserEmailRequest.newBuilder()
              .setEmail(request.getEmail())
              .build());

      // Nếu user tồn tại, trả về lỗi
      responseObserver.onNext(AuthResponse.newBuilder()
          .setSuccess(false)
          .setMessage("Email is already registered")
          .build());
      responseObserver.onCompleted();
      return;

    } catch (StatusRuntimeException e) {
      // Xử lý lỗi NOT_FOUND (User chưa tồn tại, hợp lệ cho đăng ký)
      if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
        System.out.println("User not found, proceeding with registration...");
      } else {
        // Các lỗi khác
        responseObserver.onNext(AuthResponse.newBuilder()
            .setSuccess(false)
            .setMessage("Error while checking existing user: " + e.getMessage())
            .build());
        responseObserver.onCompleted();
        return;
      }
    }

    // Tạo user mới
    UserResponse newUser = userServiceStub.createUser(
        CreateUserRequest.newBuilder()
            .setName("") // Nếu không có thông tin tên, để trống
            .setEmail(request.getEmail())
            .setPassword(passwordEncoder.encode(request.getPassword()))
            .setRole(request.getRole())
            .build());

    // Tạo token
    Authentication authentication = new UsernamePasswordAuthenticationToken(newUser.getEmail(), null);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    String token = jwtProvider.generatedToken(authentication);

    // Trả về phản hồi thành công
    responseObserver.onNext(AuthResponse.newBuilder()
        .setSuccess(true)
        .setMessage("Registration successful")
        .setEmail(newUser.getEmail())
        .setRole(newUser.getRole())
        .setToken(token)
        .build());
    responseObserver.onCompleted();
  }

  @Override
  public void login(LoginRequest request, StreamObserver<AuthResponse> responseObserver) {
    UserResponse user = userServiceStub.getUserByEmail(
        UserEmailRequest.newBuilder()
            .setEmail(request.getEmail())
            .build());

    if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      responseObserver.onNext(AuthResponse.newBuilder()
          .setSuccess(false)
          .setMessage("Invalid email or password")
          .build());
      responseObserver.onCompleted();
      return;
    }

    Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    String token = jwtProvider.generatedToken(authentication);

    responseObserver.onNext(AuthResponse.newBuilder()
        .setSuccess(true)
        .setMessage("Login successful")
        .setEmail(user.getEmail())
        .setRole(user.getRole())
        .setToken(token)
        .build());
    responseObserver.onCompleted();
  }
}
