package com.example.graphql_gateway.resolver;

import com.example.graphql_gateway.service.UserGrpcClient;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import user.UserResponse;

@Component
public class UserResolver implements DataFetcher<Mono<UserResponse>> {

  private final UserGrpcClient userGrpcClient;
  private static final Logger logger = LoggerFactory.getLogger(UserResolver.class);

  public UserResolver(UserGrpcClient userGrpcClient) {
    this.userGrpcClient = userGrpcClient;
  }

  @Override
  public Mono<UserResponse> get(DataFetchingEnvironment environment) {
    String email = environment.getArgument("email");
    logger.info("Fetching user for email: {}", email);
    return userGrpcClient.getUserByEmail(email)
        .doOnNext(user -> logger.info("Fetched user: {}", user))
        .doOnError(error -> logger.error("Error fetching user: {}", error.getMessage()));
  }
}
