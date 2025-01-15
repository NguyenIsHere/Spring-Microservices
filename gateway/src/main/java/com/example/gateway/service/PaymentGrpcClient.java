package com.example.gateway.service;

import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import payment.PaymentServiceGrpc;
import payment.CreatePaymentRequest;
import payment.PaymentResponse;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentGrpcClient {

  private static final Logger logger = LoggerFactory.getLogger(PaymentGrpcClient.class);

  @GrpcClient("paymentServiceChannel") // Tên channel được định nghĩa trong application.properties
  private PaymentServiceGrpc.PaymentServiceBlockingStub paymentServiceStub;

  /**
   * Gửi yêu cầu tạo giao dịch thanh toán mới.
   * 
   * @param orderId     ID của đơn hàng cần thanh toán.
   * @param callbackUrl URL callback để nhận thông báo từ ZaloPay.
   */
  public void createPayment(String orderId, String callbackUrl) {
    try {
      // Tạo gRPC request
      CreatePaymentRequest request = CreatePaymentRequest.newBuilder()
          .setOrderId(orderId)
          .setCallbackUrl(callbackUrl)
          .build();

      // Gọi Payment Service qua gRPC
      PaymentResponse response = paymentServiceStub.createPayment(request);

      // Log thông tin phản hồi
      logger.info("Payment created successfully: paymentId={}, orderId={}, amount={}, status={}, createdAt={}",
          response.getId(), response.getOrderId(), response.getAmount(), response.getStatus(),
          response.getCreatedAt());
    } catch (StatusRuntimeException e) {
      logger.error("Failed to create payment for orderId: {}. Error: {}", orderId, e.getMessage(), e);
      throw new RuntimeException("Failed to create payment", e);
    }
  }

  /**
   * Xử lý callback từ ZaloPay.
   * 
   * @param callbackData Dữ liệu callback từ ZaloPay.
   * @return Map phản hồi với thông tin xử lý kết quả.
   */
  public Map<String, Object> handleCallback(String data, String mac) {
    try {
      payment.ZaloPayCallbackRequest request = payment.ZaloPayCallbackRequest.newBuilder()
          .setData(data)
          .setMac(mac)
          .build();

      payment.ZaloPayCallbackResponse response = paymentServiceStub.handleZaloPayCallback(request);

      Map<String, Object> result = new HashMap<>();
      result.put("return_code", response.getReturnCode());
      result.put("return_message", response.getReturnMessage());
      return result;
    } catch (Exception e) {
      logger.error("Error while handling ZaloPay callback: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to handle ZaloPay callback.", e);
    }
  }

}
