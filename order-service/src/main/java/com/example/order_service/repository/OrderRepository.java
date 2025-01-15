package com.example.order_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.order_service.model.Order;

import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

  /**
   * Tìm danh sách đơn hàng theo userId.
   *
   * @param userId ID của user
   * @return Danh sách đơn hàng của user
   */
  List<Order> findByUserId(String userId);
}
