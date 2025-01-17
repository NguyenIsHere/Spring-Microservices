package com.example.graphql_gateway.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import user.UserEmailRequest;
import user.UserResponse;
import user.UserServiceGrpc;

@Service
public class UserGrpcClient {

  @GrpcClient("userServiceChannel")
  private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

  public UserResponse getUserByEmail(String email) {
    UserEmailRequest request = UserEmailRequest.newBuilder()
        .setEmail(email)
        .build();
    return userServiceStub.getUserByEmail(request);
  }
}
