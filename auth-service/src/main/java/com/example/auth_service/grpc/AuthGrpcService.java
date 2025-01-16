package com.example.auth_service.grpc;

import com.example.auth_service.model.Role;
import com.example.auth_service.service.UserServiceGrpcClient;
import com.example.auth_service.util.JwtProvider;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import auth.*;
import user.UserResponse;

@GrpcService
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

  private final UserServiceGrpcClient userServiceGrpcClient;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;

  public AuthGrpcService(UserServiceGrpcClient userServiceGrpcClient, PasswordEncoder passwordEncoder,
      JwtProvider jwtProvider) {
    this.userServiceGrpcClient = userServiceGrpcClient;
    this.passwordEncoder = passwordEncoder;
    this.jwtProvider = jwtProvider;
  }

  @Override
  public void register(RegisterRequest request, StreamObserver<AuthResponse> responseObserver) {
    // Kiểm tra vai trò hợp lệ
    if (!Role.isValidRole(request.getRole())) {
      responseObserver.onNext(AuthResponse.newBuilder()
          .setSuccess(false)
          .setMessage("Invalid role provided")
          .build());
      responseObserver.onCompleted();
      return;
    }

    // Gọi gRPC đến user-service để kiểm tra email
    UserResponse existingUser = userServiceGrpcClient.getUserByEmail(request.getEmail());
    if (existingUser != null) {
      responseObserver.onNext(AuthResponse.newBuilder()
          .setSuccess(false)
          .setMessage("Email is already registered")
          .build());
      responseObserver.onCompleted();
      return;
    }

    // Tạo CreateUserRequest để gửi đến user-service
    user.CreateUserRequest createUserRequest = user.CreateUserRequest.newBuilder()
        .setName("") // Nếu không có thông tin tên, để trống
        .setEmail(request.getEmail())
        .setPassword(passwordEncoder.encode(request.getPassword())) // Mã hóa mật khẩu
        .setRole(request.getRole())
        .build();

    // Gửi request để tạo user mới
    UserResponse newUser = userServiceGrpcClient.createUser(createUserRequest);

    // Tạo token JWT
    Authentication authentication = new UsernamePasswordAuthenticationToken(newUser.getEmail(), null);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    String token = jwtProvider.generatedToken(authentication);

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
    UserResponse user = userServiceGrpcClient.getUserByEmail(request.getEmail());
    if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      responseObserver.onNext(AuthResponse.newBuilder()
          .setSuccess(false)
          .setMessage("Invalid email or password")
          .build());
      responseObserver.onCompleted();
      return;
    }

    // Authenticate the user and generate JWT
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

  @Override
  public void verifyToken(VerifyTokenRequest request, StreamObserver<VerifyTokenResponse> responseObserver) {
    try {
      // Extract email and authorities (role) from token
      String email = jwtProvider.getEmailFromJwt(request.getToken());
      String role = jwtProvider.getRoleFromJwt(request.getToken()); // You can implement this method in JwtProvider

      responseObserver.onNext(VerifyTokenResponse.newBuilder()
          .setEmail(email)
          .setRole(role)
          .setIsValid(true)
          .build());
    } catch (Exception e) {
      responseObserver.onNext(VerifyTokenResponse.newBuilder()
          .setIsValid(false)
          .build());
    }
    responseObserver.onCompleted();
  }
}
