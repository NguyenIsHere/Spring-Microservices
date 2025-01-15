package com.example.gateway.controller;

import com.example.gateway.dto.CreateOrderDTO;
import com.example.gateway.dto.UpdateOrderStatusDTO;
import com.example.gateway.service.OrderGrpcClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
  public ResponseEntity<String> createOrder(@RequestBody CreateOrderDTO createOrderDTO) {

    logger.info("Received create order request: {}", createOrderDTO);

    // Gửi yêu cầu tới Order Service qua gRPC
    orderGrpcClient.createOrder(createOrderDTO);

    // Trả về phản hồi tạm thời
    return ResponseEntity.accepted().body("Your request is being processed.");
  }

  /**
   * Gửi yêu cầu lấy danh sách đơn hàng của một user.
   * Trả về thông báo tạm thời: "Request is being processed."
   */
  @GetMapping("/{userId}")
  public ResponseEntity<String> getOrdersByUserId(@PathVariable String userId) {
    // Gửi yêu cầu tới Order Service qua gRPC
    orderGrpcClient.getOrdersByUserId(userId);

    // Trả về phản hồi tạm thời
    return ResponseEntity.accepted().body("Your request is being processed.");
  }

  /**
   * Gửi yêu cầu cập nhật trạng thái đơn hàng.
   * Trả về thông báo tạm thời: "Request is being processed."
   */
  @PatchMapping("/{orderId}/status")
  public ResponseEntity<String> updateOrderStatus(@PathVariable String orderId,
      @RequestBody UpdateOrderStatusDTO updateOrderStatusDTO) {
    // Gửi yêu cầu tới Order Service qua gRPC
    orderGrpcClient.updateOrderStatus(orderId, updateOrderStatusDTO);

    // Trả về phản hồi tạm thời
    return ResponseEntity.accepted().body("Your request is being processed.");
  }
}
