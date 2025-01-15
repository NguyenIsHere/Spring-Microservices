package com.example.payment_service.repository;

import com.example.payment_service.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {

  // Tìm thanh toán theo orderId
  Optional<Payment> findByOrderId(String orderId);

  // Kiểm tra xem thanh toán đã tồn tại cho orderId hay chưa
  boolean existsByOrderId(String orderId);
}
