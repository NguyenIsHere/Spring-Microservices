package com.example.graphql_gateway.resolver;

import com.example.graphql_gateway.service.UserGrpcClient;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import org.springframework.stereotype.Component;

@Component
public class UserResolver implements DataFetcher<Object> {

  private final UserGrpcClient userGrpcClient;

  public UserResolver(UserGrpcClient userGrpcClient) {
    this.userGrpcClient = userGrpcClient;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    String email = environment.getArgument("email");
    return userGrpcClient.getUserByEmail(email);
  }
}