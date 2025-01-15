package com.example.gateway.controller;

import com.example.gateway.dto.CreatePaymentDTO;
import com.example.gateway.service.PaymentGrpcClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

  private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

  @Autowired
  private PaymentGrpcClient paymentGrpcClient;

  /**
   * Tạo giao dịch thanh toán mới.
   *
   * @param createPaymentDTO DTO chứa thông tin tạo thanh toán.
   * @return Phản hồi xác nhận việc xử lý yêu cầu.
   */
  @PostMapping
  public ResponseEntity<String> createPayment(@RequestBody CreatePaymentDTO createPaymentDTO) {
    logger.info("Received create payment request: {}", createPaymentDTO);

    // Gửi yêu cầu tới Payment Service qua gRPC
    paymentGrpcClient.createPayment(createPaymentDTO.getOrderId(), createPaymentDTO.getCallback_url());

    // Trả về phản hồi tạm thời
    return ResponseEntity.accepted().body("Your payment request is being processed.");
  }

  /**
   * Xử lý callback từ ZaloPay.
   *
   * @param callbackData Dữ liệu từ callback của ZaloPay.
   * @return Phản hồi xác nhận việc xử lý callback.
   */
  @PostMapping("/callback")
  public ResponseEntity<Map<String, Object>> handleCallback(@RequestBody Map<String, Object> callbackData) {
    logger.info("Received callback data from ZaloPay: {}", callbackData);

    // Tách dữ liệu callback
    String data = (String) callbackData.get("data");
    String mac = (String) callbackData.get("mac");

    // Gửi callback data tới Payment Service qua gRPC
    Map<String, Object> response = paymentGrpcClient.handleCallback(data, mac);

    // Trả về phản hồi
    return ResponseEntity.ok(response);
  }
}
