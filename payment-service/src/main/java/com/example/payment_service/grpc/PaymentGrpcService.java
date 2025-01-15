package com.example.payment_service.grpc;

import com.example.payment_service.model.Payment;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.vn.zalopay.crypto.HMACUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;

import order.OrderServiceGrpc;
import payment.CreatePaymentRequest;
import payment.GetPaymentByOrderIdRequest;
import payment.PaymentResponse;
import payment.UpdatePaymentStatusRequest;
import payment.ZaloPayCallbackResponse;
import websocket.WebSocketServiceGrpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@GrpcService
public class PaymentGrpcService extends payment.PaymentServiceGrpc.PaymentServiceImplBase {

  private static final Logger logger = LoggerFactory.getLogger(PaymentGrpcService.class);

  @Autowired
  private PaymentRepository paymentRepository;

  @GrpcClient("orderServiceChannel")
  private OrderServiceGrpc.OrderServiceBlockingStub orderServiceStub;

  @GrpcClient("webSocketGatewayChannel")
  private WebSocketServiceGrpc.WebSocketServiceBlockingStub webSocketServiceStub;

  private static final Map<String, String> config = new HashMap<>() {
    {
      put("app_id", "2553");
      put("key1", "PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL");
      put("key2", "kLtgPl8HHhfvMuDHPwKfgfsY4Ydm9eIz");
      put("endpoint", "https://sb-openapi.zalopay.vn/v2/create");
    }
  };

  /**
   * Tạo thanh toán mới
   */
  // @Override
  // public void createPayment(CreatePaymentRequest request,
  // StreamObserver<PaymentResponse> responseObserver) {
  // try {
  // String orderId = request.getOrderId();
  // String callbackUrl = request.getCallbackUrl();

  // logger.info("Received createPayment request for orderId: {}", orderId);

  // // Gọi gRPC đến order-service để lấy thông tin đơn hàng
  // var orderResponse = orderServiceStub.getOrderById(
  // order.GetOrderRequest.newBuilder().setOrderId(orderId).build());

  // if (!orderResponse.hasOrder()) {
  // logger.error("Order not found for orderId: {}", orderId);
  // responseObserver.onError(io.grpc.Status.NOT_FOUND
  // .withDescription("Order not found")
  // .asRuntimeException());
  // return;
  // }

  // var order = orderResponse.getOrder();
  // double totalAmount = order.getTotalPrice();
  // String userId = order.getUserId();

  // // Tạo app_trans_id
  // String appTransId = generateAppTransId();

  // // Tạo thanh toán mới trong MongoDB
  // Payment payment = new Payment();
  // payment.setId(appTransId); // Gán app_trans_id vào id
  // payment.setOrderId(orderId);
  // payment.setUserId(userId);
  // payment.setAmount(totalAmount);
  // payment.setStatus("PENDING");
  // payment.setCreatedAt(new java.util.Date());
  // payment.setUpdatedAt(new java.util.Date());

  // Payment savedPayment = paymentRepository.save(payment);

  // // Chuẩn bị request gửi tới ZaloPay
  // Map<String, Object> zalopayRequest = prepareZaloPayRequest(savedPayment,
  // callbackUrl);

  // // Gửi yêu cầu tới ZaloPay
  // String response = sendRequestToZaloPay(zalopayRequest);
  // String orderUrl = extractOrderUrl(response);

  // // Gửi thông báo qua WebSocket Gateway
  // sendOrderUrlToWebSocketGateway(userId, orderUrl);

  // logger.info("Payment created successfully for orderId: {}", orderId);

  // // Trả về phản hồi gRPC
  // responseObserver.onNext(buildPaymentResponse(savedPayment));
  // responseObserver.onCompleted();

  // } catch (Exception e) {
  // logger.error("Failed to create payment: {}", e.getMessage(), e);
  // responseObserver.onError(io.grpc.Status.INTERNAL
  // .withDescription("Failed to create payment")
  // .augmentDescription(e.getMessage())
  // .asRuntimeException());
  // }
  // }

  @Override
  public void createPayment(CreatePaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
    try {
      String orderId = request.getOrderId();
      String callbackUrl = request.getCallbackUrl();

      logger.info("Received createPayment request for orderId: {}", orderId);

      // Gọi phương thức chung
      String orderUrl = processPayment(orderId, callbackUrl);

      // Trả về phản hồi gRPC
      responseObserver.onNext(PaymentResponse.newBuilder()
          .setOrderUrl(orderUrl)
          .setMessage("Payment created successfully")
          .build());
      responseObserver.onCompleted();

    } catch (Exception e) {
      logger.error("Failed to create payment: {}", e.getMessage(), e);
      responseObserver.onError(io.grpc.Status.INTERNAL
          .withDescription("Failed to create payment")
          .augmentDescription(e.getMessage())
          .asRuntimeException());
    }
  }

  public String processPayment(String orderId, String callbackUrl) {
    try {
      logger.info("Processing payment for orderId: {}", orderId);

      // Gọi gRPC đến order-service để lấy thông tin đơn hàng
      var orderResponse = orderServiceStub.getOrderById(
          order.GetOrderRequest.newBuilder().setOrderId(orderId).build());

      if (!orderResponse.hasOrder()) {
        logger.error("Order not found for orderId: {}", orderId);
        throw new RuntimeException("Order not found for orderId: " + orderId);
      }

      var order = orderResponse.getOrder();
      double totalAmount = order.getTotalPrice();
      String userId = order.getUserId();

      // Tạo app_trans_id
      String appTransId = generateAppTransId();

      // Tạo thanh toán mới trong MongoDB
      Payment payment = new Payment();
      payment.setId(appTransId); // Gán app_trans_id vào id
      payment.setOrderId(orderId);
      payment.setUserId(userId);
      payment.setAmount(totalAmount);
      payment.setStatus("PENDING");
      payment.setCreatedAt(new java.util.Date());
      payment.setUpdatedAt(new java.util.Date());

      Payment savedPayment = paymentRepository.save(payment);

      // Chuẩn bị request gửi tới ZaloPay
      Map<String, Object> zalopayRequest = prepareZaloPayRequest(savedPayment, callbackUrl);

      // Gửi yêu cầu tới ZaloPay
      String response = sendRequestToZaloPay(zalopayRequest);
      String orderUrl = extractOrderUrl(response);

      // Gửi thông báo qua WebSocket Gateway
      sendOrderUrlToWebSocketGateway(userId, orderUrl);

      logger.info("Payment successfully processed for orderId: {}", orderId);

      // Trả về orderUrl
      return orderUrl;

    } catch (Exception e) {
      logger.error("Failed to process payment for orderId: {}. Error: {}", orderId, e.getMessage(), e);
      throw new RuntimeException("Failed to process payment", e);
    }
  }

  /**
   * Lấy thanh toán theo orderId
   */
  @Override
  public void getPaymentByOrderId(GetPaymentByOrderIdRequest request,
      StreamObserver<PaymentResponse> responseObserver) {
    String orderId = request.getOrderId();
    logger.info("Received getPaymentByOrderId request for orderId: {}", orderId);

    Optional<Payment> optionalPayment = paymentRepository.findById(orderId);

    if (optionalPayment.isEmpty()) {
      logger.error("Payment not found for orderId: {}", orderId);
      responseObserver.onError(io.grpc.Status.NOT_FOUND
          .withDescription("Payment not found")
          .asRuntimeException());
      return;
    }

    Payment payment = optionalPayment.get();
    responseObserver.onNext(buildPaymentResponse(payment));
    responseObserver.onCompleted();
  }

  /**
   * Cập nhật trạng thái thanh toán
   */
  @Override
  public void updatePaymentStatus(UpdatePaymentStatusRequest request,
      StreamObserver<PaymentResponse> responseObserver) {
    String orderId = request.getOrderId();
    String status = request.getStatus();
    logger.info("Received updatePaymentStatus request for orderId: {}, status: {}", orderId, status);

    Optional<Payment> optionalPayment = paymentRepository.findById(orderId);

    if (optionalPayment.isEmpty()) {
      logger.error("Payment not found for orderId: {}", orderId);
      responseObserver.onError(io.grpc.Status.NOT_FOUND
          .withDescription("Payment not found")
          .asRuntimeException());
      return;
    }

    Payment payment = optionalPayment.get();
    payment.setStatus(status);
    payment.setUpdatedAt(new java.util.Date());
    Payment updatedPayment = paymentRepository.save(payment);

    logger.info("Payment status updated successfully for orderId: {}", orderId);
    responseObserver.onNext(buildPaymentResponse(updatedPayment));
    responseObserver.onCompleted();
  }

  private String generateAppTransId() {
    Random rand = new Random();
    return String.format("%s_%d", getCurrentDate("yyMMdd"), rand.nextInt(1000000));
  }

  private Map<String, Object> prepareZaloPayRequest(Payment payment, String callbackUrl) {
    // Random rand = new Random();
    // int transID = rand.nextInt(1000000);

    Map<String, Object> request = new HashMap<>();
    request.put("app_id", config.get("app_id"));
    request.put("app_user", payment.getUserId());
    // request.put("app_trans_id", String.format("%s_%d", getCurrentDate("yyMMdd"),
    // transID));
    request.put("app_trans_id", payment.getId());
    request.put("app_time", System.currentTimeMillis());
    request.put("amount", (int) Math.round(payment.getAmount()));
    request.put("item", "[]"); // Placeholder for items
    request.put("description", "Payment for order #" + payment.getOrderId());
    request.put("embed_data", "{}");
    request.put("bank_code", "zalopayapp");
    request.put("callback_url", callbackUrl);
    request.put("mac", generateMac(request));
    return request;
  }

  private String sendRequestToZaloPay(Map<String, Object> request) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    request.forEach(body::add);

    HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);

    try {
      ResponseEntity<String> response = restTemplate.postForEntity(config.get("endpoint"), httpEntity, String.class);

      // Log toàn bộ response từ ZaloPay
      logger.info("ZaloPay Response: {}", response.getBody());

      return response.getBody();
    } catch (Exception e) {
      logger.error("Failed to send request to ZaloPay: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to send request to ZaloPay", e);
    }
  }

  @SuppressWarnings("unchecked")
  private String extractOrderUrl(String response) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
      return (String) responseMap.get("order_url");
    } catch (Exception e) {
      logger.error("Failed to extract order_url: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to extract order_url", e);
    }
  }

  private void sendOrderUrlToWebSocketGateway(String userId, String orderUrl) {
    try {
      websocket.NotificationRequest request = websocket.NotificationRequest.newBuilder()
          .setUserId(userId)
          .setMessage("Payment link: " + orderUrl)
          .build();

      websocket.NotificationResponse response = webSocketServiceStub.sendNotification(request);

      if (!response.getSuccess()) {
        logger.error("Failed to send notification: {}", response.getError());
      }
    } catch (Exception e) {
      logger.error("Error while sending WebSocket notification: {}", e.getMessage(), e);
    }
  }

  @Override
  public void handleZaloPayCallback(payment.ZaloPayCallbackRequest request,
      StreamObserver<payment.ZaloPayCallbackResponse> responseObserver) {
    logger.info("Received ZaloPay callback request");

    try {
      String data = request.getData();
      String requestMac = request.getMac();

      logger.info("Callback raw data: {}", data);

      // Tạo MAC từ data và so sánh với MAC nhận được
      String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, config.get("key2"), data);
      if (!mac.equals(requestMac)) {
        logger.warn("Invalid MAC in callback request");
        responseObserver.onNext(payment.ZaloPayCallbackResponse.newBuilder()
            .setReturnCode(-1)
            .setReturnMessage("MAC không hợp lệ")
            .build());
        responseObserver.onCompleted();
        return;
      }

      // Parse dữ liệu callback
      ObjectMapper objectMapper = new ObjectMapper();
      @SuppressWarnings("unchecked")
      Map<String, Object> dataJson = objectMapper.readValue(data, Map.class);
      logger.info("Parsed callback data: {}", dataJson);

      String appTransId = (String) dataJson.get("app_trans_id");

      // Tìm payment theo appTransId
      Payment payment = paymentRepository.findById(appTransId)
          .orElseThrow(() -> new RuntimeException("Payment not found for appTransId: " + appTransId));

      // Thanh toán thành công
      payment.setStatus("PAID");
      payment.setUpdatedAt(new java.util.Date());
      paymentRepository.save(payment);

      // Cập nhật trạng thái đơn hàng và các tác vụ liên quan
      processOrderItemsAndClearCart(payment.getOrderId());

      logger.info("Payment successfully processed for appTransId: {}", appTransId);

      responseObserver.onNext(ZaloPayCallbackResponse.newBuilder()
          .setReturnCode(1)
          .setReturnMessage("Xử lý callback thành công")
          .build());
    } catch (Exception e) {
      logger.error("Error while handling ZaloPay callback: {}", e.getMessage(), e);
      responseObserver.onNext(payment.ZaloPayCallbackResponse.newBuilder()
          .setReturnCode(0)
          .setReturnMessage("Lỗi trong quá trình xử lý callback")
          .build());
    } finally {
      responseObserver.onCompleted();
    }
  }

  private void processOrderItemsAndClearCart(String orderId) {
    try {
      // Gọi OrderGrpcService để thực hiện giảm số lượng sản phẩm và xóa giỏ hàng
      orderServiceStub.updateOrderStatus(order.UpdateOrderStatusRequest.newBuilder()
          .setOrderId(orderId)
          .setStatus("PAID") // Chỉ định trạng thái đơn hàng là PAID
          .build());
      logger.info("Successfully updated order status and processed related tasks for orderId: {}", orderId);
    } catch (Exception e) {
      logger.error("Failed to process order items or clear cart for orderId: {}. Error: {}", orderId, e.getMessage(),
          e);
      throw new RuntimeException("Failed to process order items or clear cart", e);
    }
  }

  private PaymentResponse buildPaymentResponse(Payment payment) {
    return PaymentResponse.newBuilder()
        .setId(payment.getId())
        .setOrderId(payment.getOrderId())
        .setStatus(payment.getStatus())
        .setAmount(payment.getAmount())
        .setCreatedAt(toGrpcTimestamp(payment.getCreatedAt()))
        .setUpdatedAt(toGrpcTimestamp(payment.getUpdatedAt()))
        .build();
  }

  private String generateMac(Map<String, Object> request) {
    String data = String.join("|",
        request.get("app_id").toString(),
        request.get("app_trans_id").toString(),
        request.get("app_user").toString(),
        request.get("amount").toString(),
        request.get("app_time").toString(),
        request.get("embed_data").toString(),
        request.get("item").toString());

    return HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, config.get("key1"), data);
  }

  private com.google.protobuf.Timestamp toGrpcTimestamp(java.util.Date date) {
    return com.google.protobuf.Timestamp.newBuilder()
        .setSeconds(date.getTime() / 1000)
        .setNanos(0)
        .build();
  }

  private static String getCurrentDate(String format) {
    return new java.text.SimpleDateFormat(format).format(new java.util.Date());
  }
}
