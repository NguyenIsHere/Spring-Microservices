package com.example.order_service.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import order.PaymentRequestEvent;

@Component
public class OrderEventPublisher {

  private static final Logger logger = LoggerFactory.getLogger(OrderEventPublisher.class);

  private final KafkaTemplate<String, PaymentRequestEvent> kafkaTemplate;

  public OrderEventPublisher(KafkaTemplate<String, PaymentRequestEvent> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Value("${kafka.topics.payment-request}")
  private String paymentRequestTopic;

  public void publishPaymentRequestEvent(PaymentRequestEvent event) {
    logger.info("Publishing payment request event for orderId: {}", event.getOrderId());
    kafkaTemplate.send(paymentRequestTopic, event);
  }
}
