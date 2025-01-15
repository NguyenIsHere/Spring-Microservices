package com.example.cart_service.model;

import product.ProductVariant; // Import từ các file biên dịch gRPC

import lombok.Data;

@Data
public class CartItem {
  private ProductVariant productVariant; // Dùng trực tiếp ProductVariant từ gRPC
  private int quantity; // Số lượng sản phẩm
  private double price; // Giá sản phẩm
}
