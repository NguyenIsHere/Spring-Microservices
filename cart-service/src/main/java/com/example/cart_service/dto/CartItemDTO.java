package com.example.cart_service.dto;

import lombok.Data;

@Data
public class CartItemDTO {
  private String productVariantId; // Chỉ lưu ID thay vì toàn bộ object để giảm tải
  private int quantity;
  private double price;
}
