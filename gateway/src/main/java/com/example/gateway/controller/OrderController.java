package com.example.gateway.controller;

import com.example.gateway.dto.CreateOrderDTO;
import com.example.gateway.dto.UpdateOrderStatusDTO;
import com.example.gateway.service.OrderGrpcClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

  private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

  private final OrderGrpcClient orderGrpcClient;

  public OrderController(OrderGrpcClient orderGrpcClient) {
    this.orderGrpcClient = orderGrpcClient;
  }

  /**
   * Gửi yêu cầu tạo đơn hàng mới.
   * Trả về thông báo tạm thời: "Request is being processed."
   */
  @PostMapping
  public Mono<ResponseEntity<String>> createOrder() {
    return getCurrentUserId()
        .flatMap(userId -> {
          // Tự khởi tạo CreateOrderDTO
          CreateOrderDTO createOrderDTO = new CreateOrderDTO();
          createOrderDTO.setUserId(userId);

          logger.info("Received create order request from userId {}: {}", userId, createOrderDTO);

          // Gửi yêu cầu tới Order Service qua gRPC
          orderGrpcClient.createOrder(createOrderDTO);

          return Mono.just(ResponseEntity.accepted().body("Your request is being processed."));
        });
  }

  /**
   * Gửi yêu cầu lấy danh sách đơn hàng của user hiện tại.
   * Trả về thông báo tạm thời: "Request is being processed."
   */
  @GetMapping
  public Mono<ResponseEntity<String>> getOrdersByUserId() {
    return getCurrentUserId()
        .doOnNext(userId -> logger.info("Fetching orders for userId: {}", userId))
        .flatMap(userId -> {
          // Gửi yêu cầu tới Order Service qua gRPC
          orderGrpcClient.getOrdersByUserId(userId);
          return Mono.just(ResponseEntity.accepted().body("Your request is being processed."));
        });
  }

  /**
   * Gửi yêu cầu cập nhật trạng thái đơn hàng.
   * Trả về thông báo tạm thời: "Request is being processed."
   */
  @PatchMapping("/{orderId}/status")
  public ResponseEntity<String> updateOrderStatus(@PathVariable String orderId,
      @RequestBody UpdateOrderStatusDTO updateOrderStatusDTO) {
    logger.info("Received update order status request for orderId {}: {}", orderId, updateOrderStatusDTO);

    // Gửi yêu cầu tới Order Service qua gRPC
    orderGrpcClient.updateOrderStatus(orderId, updateOrderStatusDTO);

    // Trả về phản hồi tạm thời
    return ResponseEntity.accepted().body("Your request is being processed.");
  }

  /**
   * Lấy `userId` từ SecurityContext.
   */
  private Mono<String> getCurrentUserId() {
    return ReactiveSecurityContextHolder.getContext()
        .map(context -> {
          Authentication authentication = context.getAuthentication();
          if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("User not authenticated");
          }
          // Lấy userId từ details
          Object userId = authentication.getDetails();
          if (userId == null || !(userId instanceof String)) {
            throw new IllegalStateException("User ID not found in authentication details");
          }
          return (String) userId;
        });
  }
}
