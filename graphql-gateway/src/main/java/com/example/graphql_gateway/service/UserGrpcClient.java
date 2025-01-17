package com.example.graphql_gateway.service;

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