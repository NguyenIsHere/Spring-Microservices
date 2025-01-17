package com.example.graphql_gateway.resolver;

import com.example.graphql_gateway.dto.UserDTO;
import com.example.graphql_gateway.service.UserGrpcClient;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import user.UserResponse;

import org.springframework.stereotype.Component;

@Component
public class UserResolver implements DataFetcher<UserResponse> {

  private final UserGrpcClient userGrpcClient;

  public UserResolver(UserGrpcClient userGrpcClient) {
    this.userGrpcClient = userGrpcClient;
  }

  @Override
  public UserResponse get(DataFetchingEnvironment environment) {
    String email = environment.getArgument("email");
    System.out.println("Fetching user with email: " + email);

    UserResponse response = userGrpcClient.getUserByEmail(email);

    System.out.println("Resolver response: " + response);

    return response;
  }
}
