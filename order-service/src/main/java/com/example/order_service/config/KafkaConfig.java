package com.example.order_service.config;

import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import order.PaymentRequestEvent;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

  @Bean
  public KafkaTemplate<String, PaymentRequestEvent> kafkaTemplate(
      ProducerFactory<String, PaymentRequestEvent> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
  }

  @Bean
  public ProducerFactory<String, PaymentRequestEvent> producerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs(), new StringSerializer(),
        new KafkaProtobufSerializer<>());
  }

  private Map<String, Object> producerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:29092");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaProtobufSerializer.class);
    props.put("schema.registry.url", "http://schema-registry:8082");

    return props;
  }
}
