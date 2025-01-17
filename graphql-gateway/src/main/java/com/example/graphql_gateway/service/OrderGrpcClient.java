package com.example.graphql_gateway.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import order.GetOrderRequest;
import order.GetOrdersByUserIdRequest;
import order.OrderResponse;
import order.OrdersResponse;
import order.OrderServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OrderGrpcClient {
  private static final Logger logger = LoggerFactory.getLogger(OrderGrpcClient.class);

  @GrpcClient("orderServiceChannel")
  private OrderServiceGrpc.OrderServiceBlockingStub orderServiceStub;

  public Mono<OrderResponse> getOrderById(String orderId) {
    return Mono.create(sink -> {
      try {
        GetOrderRequest request = GetOrderRequest.newBuilder().setOrderId(orderId).build();
        logger.debug("Sending gRPC request for orderId: {}", orderId);
        OrderResponse response = orderServiceStub.getOrderById(request);
        logger.debug("Received gRPC response: {}", response);
        sink.success(response);
      } catch (Exception e) {
        logger.error("Error fetching order by ID", e);
        sink.error(e);
      }
    });
  }

  public Mono<OrdersResponse> getOrdersByUserId(String userId) {
    return Mono.create(sink -> {
      try {
        GetOrdersByUserIdRequest request = GetOrdersByUserIdRequest.newBuilder().setUserId(userId).build();
        logger.debug("Sending gRPC request for userId: {}", userId);
        OrdersResponse response = orderServiceStub.getOrdersByUserId(request);
        logger.debug("Received gRPC response: {}", response);
        sink.success(response);
      } catch (Exception e) {
        logger.error("Error fetching orders by user ID", e);
        sink.error(e);
      }
    });
  }
}
