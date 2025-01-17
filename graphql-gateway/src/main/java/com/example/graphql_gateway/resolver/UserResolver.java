package com.example.graphql_gateway.resolver;

import com.example.graphql_gateway.dto.UserDTO;
import com.example.graphql_gateway.service.UserGrpcClient;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import user.UserResponse;

import org.springframework.stereotype.Component;

@Component
public class UserResolver implements DataFetcher<UserDTO> {

  private final UserGrpcClient userGrpcClient;

  public UserResolver(UserGrpcClient userGrpcClient) {
    this.userGrpcClient = userGrpcClient;
  }

  @Override
  public UserDTO get(DataFetchingEnvironment environment) {
    String email = environment.getArgument("email");
    UserResponse response = userGrpcClient.getUserByEmail(email);
    return mapToUserDTO(response);
  }

  private UserDTO mapToUserDTO(UserResponse response) {
    UserDTO userDTO = new UserDTO();
    userDTO.setName(response.getName());
    userDTO.setEmail(response.getEmail());
    userDTO.setUserId(response.getUserId());
    userDTO.setRole(response.getRole());
    return userDTO;
  }
}
