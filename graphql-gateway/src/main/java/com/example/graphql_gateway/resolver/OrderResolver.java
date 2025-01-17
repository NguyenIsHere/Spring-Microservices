package com.example.graphql_gateway.resolver;

import com.example.graphql_gateway.service.OrderGrpcClient;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.stereotype.Component;

import order.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OrderResolver implements GraphQLQueryResolver {
  private final OrderGrpcClient orderGrpcClient;

  public OrderResolver(OrderGrpcClient orderGrpcClient) {
    this.orderGrpcClient = orderGrpcClient;
  }

  public Mono<Order> getOrderById(String orderId) {
    return orderGrpcClient.getOrderById(orderId)
        .map(orderResponse -> orderResponse.getOrder());
  }

  public Flux<Order> getOrdersByUserId(String userId) {
    return orderGrpcClient.getOrdersByUserId(userId)
        .flatMapMany(ordersResponse -> Flux.fromIterable(ordersResponse.getOrdersList()));
  }
}
