package com.example.graphql_gateway.resolver;

import com.example.graphql_gateway.service.OrderGrpcClient;
import com.example.graphql_gateway.service.UserGrpcClient;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import order.Order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import user.UserResponse;

@Component
public class UserResolver implements DataFetcher<Mono<UserResponse>> {

  private final UserGrpcClient userGrpcClient;
  private final OrderGrpcClient orderGrpcClient;

  private static final Logger logger = LoggerFactory.getLogger(UserResolver.class);

  public UserResolver(UserGrpcClient userGrpcClient, OrderGrpcClient orderGrpcClient) {
    this.userGrpcClient = userGrpcClient;
    this.orderGrpcClient = orderGrpcClient;
  }

  @Override
  public Mono<UserResponse> get(DataFetchingEnvironment environment) {
    String email = environment.getArgument("email");
    logger.info("Fetching user for email: {}", email);
    return userGrpcClient.getUserByEmail(email)
        .doOnNext(user -> logger.info("Fetched user: {}", user))
        .doOnError(error -> logger.error("Error fetching user: {}", error.getMessage()));
  }

  public Flux<Order> getOrders(UserResponse user) {
    return orderGrpcClient.getOrdersByUserId(user.getUserId())
        .flatMapMany(ordersResponse -> Flux.fromIterable(ordersResponse.getOrdersList()));
  }
}
