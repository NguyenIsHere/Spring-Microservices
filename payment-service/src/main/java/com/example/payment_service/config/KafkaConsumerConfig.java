package com.example.payment_service.config;

import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import order.PaymentRequestEvent;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, PaymentRequestEvent> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, PaymentRequestEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    return factory;
  }

  @Bean
  public DefaultKafkaConsumerFactory<String, PaymentRequestEvent> consumerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:29092"); // Thay đổi nếu Kafka chạy trong Docker
    config.put(ConsumerConfig.GROUP_ID_CONFIG, "payment-service-group");
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class);
    config.put("schema.registry.url", "http://schema-registry:8082"); // Thay đổi nếu Schema Registry chạy trong Docker
    config.put("specific.protobuf.value.type", PaymentRequestEvent.class.getName()); // Xác định lớp cụ thể của message
    return new DefaultKafkaConsumerFactory<>(config);
  }
}
