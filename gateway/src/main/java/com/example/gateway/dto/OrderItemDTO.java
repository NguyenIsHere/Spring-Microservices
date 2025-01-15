package com.example.gateway.dto;

import lombok.Data;

@Data
public class OrderItemDTO {
  private String productVariantId;
  private String name;
  private String color;
  private String size;
  private String imageUrl;
  private int quantity;
  private double price;
  private double totalPrice;
}
