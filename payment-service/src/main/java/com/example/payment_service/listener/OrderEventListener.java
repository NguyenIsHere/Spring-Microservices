package com.example.payment_service.listener;

import com.example.payment_service.model.Payment;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.vn.zalopay.crypto.HMACUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.devh.boot.grpc.client.inject.GrpcClient;
import order.PaymentRequestEvent;
import order.OrderItem;
import websocket.WebSocketServiceGrpc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class OrderEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);

    @Autowired
    private PaymentRepository paymentRepository;

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

    @Value("${zalopay.callback.url}")
    private String callbackUrl;

    @KafkaListener(topics = "${kafka.topics.payment-request}", groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentRequest(PaymentRequestEvent event) {
        try {
            logger.info("Received payment request event for orderId: {}", event.getOrderId());

            // Tạo app_trans_id
            String appTransId = generateAppTransId();

            // Tạo thanh toán mới trong MongoDB
            Payment payment = new Payment();
            payment.setId(appTransId); // Gán app_trans_id vào id
            payment.setOrderId(event.getOrderId());
            payment.setUserId(event.getUserId());
            payment.setAmount(event.getTotalPrice());
            payment.setStatus("PENDING");
            payment.setCreatedAt(new java.util.Date());
            payment.setUpdatedAt(new java.util.Date());

            Payment savedPayment = paymentRepository.save(payment);

            // Chuẩn bị request gửi tới ZaloPay với danh sách items từ event
            List<OrderItem> items = event.getItemsList(); // Lấy danh sách items
            // Chuẩn bị request gửi tới ZaloPay
            Map<String, Object> zalopayRequest = prepareZaloPayRequest(savedPayment, callbackUrl, items);

            // Gửi yêu cầu tới ZaloPay
            String response = sendRequestToZaloPay(zalopayRequest);
            String orderUrl = extractOrderUrl(response);

            // Gửi thông báo qua WebSocket Gateway
            sendOrderUrlToWebSocketGateway(event.getUserId(), orderUrl);

            logger.info("Payment successfully processed for orderId: {}", event.getOrderId());

        } catch (Exception e) {
            logger.error("Failed to process payment request event for orderId: {}. Error: {}", event.getOrderId(),
                    e.getMessage(), e);
        }
    }

    private String convertOrderItemsToJson(List<OrderItem> items) {
        return items.stream()
                .map(item -> String.format("{\"name\":\"%s\", \"price\":%.2f, \"quantity\":%d}",
                        item.getName(), item.getPrice(), item.getQuantity()))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String generateAppTransId() {
        Random rand = new Random();
        return String.format("%s_%d", getCurrentDate("yyMMdd"), rand.nextInt(1000000));
    }

    private Map<String, Object> prepareZaloPayRequest(Payment payment, String callbackUrl, List<OrderItem> items) {
        Map<String, Object> request = new HashMap<>();
        request.put("app_id", config.get("app_id"));
        request.put("app_user", payment.getUserId());
        request.put("app_trans_id", payment.getId());
        request.put("app_time", System.currentTimeMillis());
        request.put("amount", (int) Math.round(payment.getAmount()));

        // Chuyển đổi danh sách OrderItem thành JSON và gán vào item
        String itemsJson = convertOrderItemsToJson(items);
        request.put("item", itemsJson);

        request.put("description", "Payment for order #" + payment.getOrderId());
        request.put("embed_data", "{}");
        request.put("bank_code", "zalopayapp");
        request.put("callback_url", callbackUrl);
        request.put("mac", generateMac(request));
        return request;
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

    private String sendRequestToZaloPay(Map<String, Object> request) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        request.forEach(body::add);

        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(config.get("endpoint"), httpEntity,
                    String.class);

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

    private static String getCurrentDate(String format) {
        return new java.text.SimpleDateFormat(format).format(new java.util.Date());
    }
}
