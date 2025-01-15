package com.example.gateway.service;

import org.springframework.stereotype.Service;
import net.devh.boot.grpc.client.inject.GrpcClient;
import user.CreateUserRequest;
import user.UpdateUserRequest;
import user.UserEmailRequest;
import user.UserResponse;
import user.UserServiceGrpc;

import com.example.gateway.dto.UserResponseDTO;

@Service
public class UserGrpcClient {

  @GrpcClient("userServiceChannel") // Tên channel gRPC trong application.properties
  private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

  /**
   * Lấy thông tin user theo email.
   */
  public UserResponseDTO getUserByEmail(String email) {
    UserEmailRequest request = UserEmailRequest.newBuilder()
        .setEmail(email)
        .build();

    UserResponse response = userServiceStub.getUserByEmail(request);
    return mapToUserResponseDTO(response);
  }

  /**
   * Tạo user mới.
   */
  public UserResponseDTO createUser(String name, String email, String password) {
    CreateUserRequest request = CreateUserRequest.newBuilder()
        .setName(name)
        .setEmail(email)
        .setPassword(password)
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

  private UserResponseDTO mapToUserResponseDTO(UserResponse response) {
    UserResponseDTO dto = new UserResponseDTO();
    dto.setName(response.getName());
    dto.setEmail(response.getEmail());
    dto.setPassword(response.getPassword());
    return dto;
  }
}
