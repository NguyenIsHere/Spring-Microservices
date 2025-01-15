package com.example.cart_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "carts")
public class Cart {
  @Id
  private String id;

  private String userId; // ID hoặc email người dùng
  private List<CartItem> items = new ArrayList<>(); // Khởi tạo danh sách mặc định

  private double totalPrice; // Tổng giá trị của giỏ hàng
  private int totalQuantity; // Tổng số lượng sản phẩm
}
