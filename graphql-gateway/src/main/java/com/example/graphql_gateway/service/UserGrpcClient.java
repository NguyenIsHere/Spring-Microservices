package com.example.graphql_gateway.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import user.UserEmailRequest;
import user.UserResponse;
import user.UserServiceGrpc;

@Service
public class UserGrpcClient {
  private static final Logger logger = LoggerFactory.getLogger(UserGrpcClient.class);

  @GrpcClient("userServiceChannel")
  private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

  // public UserResponse getUserByEmail(String email) {
  // try {
  // if (email == null || email.trim().isEmpty()) {
  // logger.error("Email parameter is null or empty");
  // throw new IllegalArgumentException("Email cannot be null or empty");
  // }

  // UserEmailRequest request = UserEmailRequest.newBuilder()
  // .setEmail(email)
  // .build();

  // // Log the outgoing request
  // logger.debug("Sending gRPC request for email: {}", email);

  // UserResponse response = userServiceStub.getUserByEmail(request);

  // // Log the successful response
  // logger.debug("Received gRPC response: {}", response);

  // return response;

  // } catch (StatusRuntimeException e) {
  // Status status = e.getStatus();
  // logger.error("gRPC call failed: {} - {}", status.getCode(),
  // status.getDescription());

  // if (status.getCode() == Status.Code.NOT_FOUND) {
  // return null;
  // }

  // throw new RuntimeException("Error fetching user data: " + e.getMessage(), e);
  // } catch (Exception e) {
  // logger.error("Unexpected error in getUserByEmail", e);
  // throw new RuntimeException("Unexpected error fetching user data", e);
  // }
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
}