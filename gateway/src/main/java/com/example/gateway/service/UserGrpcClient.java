package com.example.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import net.devh.boot.grpc.client.inject.GrpcClient;
import reactor.core.publisher.Mono;
import user.CreateUserRequest;
import user.UpdateUserRequest;
import user.UserEmailRequest;
import user.UserResponse;
import user.UserServiceGrpc;

import com.example.gateway.dto.UserResponseDTO;

import io.grpc.StatusRuntimeException;

@Service
public class UserGrpcClient {

  private static final Logger logger = LoggerFactory.getLogger(UserGrpcClient.class);

  @GrpcClient("userServiceChannel") // Tên channel gRPC trong application.properties
  private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

  /**
   * Lấy thông tin user theo email.
   */
  // public UserResponseDTO getUserByEmail(String email) {
  // UserEmailRequest request = UserEmailRequest.newBuilder()
  // .setEmail(email)
  // .build();

  // UserResponse response = userServiceStub.getUserByEmail(request);
  // return mapToUserResponseDTO(response);
  // }

  public Mono<UserResponse> getUserByEmail(String email) {
    return Mono.create(sink -> {
      try {
        if (email == null || email.trim().isEmpty()) {
          logger.error("Email parameter is null or empty");
          sink.error(new IllegalArgumentException("Email cannot be null or empty"));
          return;
        }

        UserEmailRequest request = UserEmailRequest.newBuilder()
            .setEmail(email)
            .build();

        logger.debug("Sending gRPC request for email: {}", email);
        UserResponse response = userServiceStub.getUserByEmail(request);

        logger.debug("Received gRPC response: {}", response);
        sink.success(response);

      } catch (StatusRuntimeException e) {
        logger.error("gRPC call failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
        sink.error(e);
      } catch (Exception e) {
        logger.error("Unexpected error in getUserByEmail", e);
        sink.error(e);
      }
    });
  }

  /**
   * Tạo user mới.
   */
  public UserResponseDTO createUser(String name, String email, String password, String role) {
    CreateUserRequest request = CreateUserRequest.newBuilder()
        .setName(name)
        .setEmail(email)
        .setPassword(password)
        .setRole(role)
        .build();

    UserResponse response = userServiceStub.createUser(request);
    return mapToUserResponseDTO(response);
  }

  /**
   * Cập nhật thông tin user.
   */
  public UserResponseDTO updateUser(String email, String name, String password) {
    UpdateUserRequest.Builder requestBuilder = UpdateUserRequest.newBuilder().setEmail(email);

    if (name != null && !name.isEmpty()) {
      requestBuilder.setName(name);
    }

    if (password != null && !password.isEmpty()) {
      requestBuilder.setPassword(password);
    }

    UserResponse response = userServiceStub.updateUser(requestBuilder.build());
    return mapToUserResponseDTO(response);
  }

  public Mono<String> getUserIdByEmail(String email) {
    return Mono.fromCallable(() -> {
      UserEmailRequest request = UserEmailRequest.newBuilder()
          .setEmail(email)
          .build();
      UserResponse response = userServiceStub.getUserByEmail(request);
      return response.getUserId(); // Trả về userId
    });
  }

  private UserResponseDTO mapToUserResponseDTO(UserResponse response) {
    UserResponseDTO dto = new UserResponseDTO();
    dto.setName(response.getName());
    dto.setEmail(response.getEmail());
    dto.setPassword(response.getPassword());
    dto.setRole(response.getRole());
    dto.setUserId(response.getUserId()); // Gắn userId
    return dto;
  }

}
