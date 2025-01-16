package com.example.auth_service.service;

import user.*;
import net.devh.boot.grpc.client.inject.GrpcClient;

import org.springframework.stereotype.Service;

@Service
public class UserServiceGrpcClient {

  @GrpcClient("userServiceChannel")
  private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

  public UserServiceGrpcClient(UserServiceGrpc.UserServiceBlockingStub userServiceStub) {
    this.userServiceStub = userServiceStub;
  }

  public UserResponse createUser(CreateUserRequest request) {
    return userServiceStub.createUser(request);
  }

  public UserResponse getUserByEmail(String email) {
    return userServiceStub.getUserByEmail(
        UserEmailRequest.newBuilder()
            .setEmail(email)
            .build());
  }

}
