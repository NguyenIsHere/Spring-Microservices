package com.example.gateway.service;

import com.example.gateway.dto.CreateOrderDTO;
import com.example.gateway.dto.UpdateOrderStatusDTO;

import order.CreateOrderRequest;
import order.OrderServiceGrpc;
import order.GetOrdersByUserIdRequest;
import order.UpdateOrderStatusRequest;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderGrpcClient {

  private static final Logger logger = LoggerFactory.getLogger(OrderGrpcClient.class);

  @GrpcClient("orderServiceChannel") // Tên channel được cấu hình trong application.properties
  private OrderServiceGrpc.OrderServiceBlockingStub orderServiceStub;

  /**
   * Gửi yêu cầu tạo đơn hàng tới Order Service.
   */
  public void createOrder(CreateOrderDTO createOrderDTO) {
    try {
      CreateOrderRequest request = CreateOrderRequest.newBuilder()
          .setUserId(createOrderDTO.getUserId())
          .build();

      orderServiceStub.createOrder(request);
      logger.info("Successfully sent create order request for userId: {}", createOrderDTO.getUserId());
    } catch (StatusRuntimeException e) {
      logger.error("Error while sending create order request: {}", e.getMessage(), e);
    }
  }

  /**
   * Gửi yêu cầu lấy danh sách đơn hàng theo userId tới Order Service.
   */
  public void getOrdersByUserId(String userId) {
    try {
      GetOrdersByUserIdRequest request = GetOrdersByUserIdRequest.newBuilder()
          .setUserId(userId)
          .build();

      orderServiceStub.getOrdersByUserId(request);
      logger.info("Successfully sent get orders request for userId: {}", userId);
    } catch (StatusRuntimeException e) {
      logger.error("Error while sending get orders request: {}", e.getMessage(), e);
    }
  }

  /**
   * Gửi yêu cầu cập nhật trạng thái đơn hàng tới Order Service.
   */
  public void updateOrderStatus(String orderId, UpdateOrderStatusDTO updateOrderStatusDTO) {
    try {
      UpdateOrderStatusRequest request = UpdateOrderStatusRequest.newBuilder()
          .setOrderId(orderId)
          .setStatus(updateOrderStatusDTO.getStatus())
          .build();

      orderServiceStub.updateOrderStatus(request);
      logger.info("Successfully sent update order status request for orderId: {}", orderId);
    } catch (StatusRuntimeException e) {
      logger.error("Error while sending update order status request: {}", e.getMessage(), e);
    }
  }

  /**
   * Chuyển đổi từ OrderItemDTO sang gRPC OrderItem.
   */
  // private OrderItem toGrpcOrderItem(OrderItemDTO dto) {
  // return OrderItem.newBuilder()
  // .setProductVariantId(dto.getProductVariantId())
  // .setName(dto.getName())
  // .setColor(dto.getColor())
  // .setSize(dto.getSize())
  // .setImageUrl(dto.getImageUrl())
  // .setQuantity(dto.getQuantity())
  // .setPrice(dto.getPrice())
  // .setTotalPrice(dto.getTotalPrice())
  // .build();
  // }
}
