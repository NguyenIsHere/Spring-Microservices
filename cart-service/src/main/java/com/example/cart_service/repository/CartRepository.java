package com.example.cart_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.cart_service.model.Cart;
import java.util.Optional;

public interface CartRepository extends MongoRepository<Cart, String> {
  /**
   * Tìm giỏ hàng theo userId.
   * 
   * @param userId ID hoặc email của người dùng
   * @return Optional<Cart> nếu giỏ hàng tồn tại
   */
  Optional<Cart> findByUserId(String userId);
}
