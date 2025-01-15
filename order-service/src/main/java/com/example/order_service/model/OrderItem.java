package com.example.order_service.model;

import lombok.Data;

@Data
public class OrderItem {
  private String productVariantId; // ID của ProductVariant
  private String name; // Tên sản phẩm tại thời điểm đặt hàng
  private String color; // Màu sản phẩm
  private String size; // Kích thước sản phẩm
  private String imageUrl; // Ảnh sản phẩm
  private int quantity; // Số lượng sản phẩm
  private double price; // Giá của 1 sản phẩm tại thời điểm đặt hàng
  private double totalPrice; // Tổng giá trị = quantity * price
}
