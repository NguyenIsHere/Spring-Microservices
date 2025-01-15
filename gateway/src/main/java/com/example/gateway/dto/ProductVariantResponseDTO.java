package com.example.gateway.dto;

import lombok.Data;

@Data
public class ProductVariantResponseDTO {
  private String id;
  private double price;
  private int quantity;
  private String color;
  private String size;
  private String imageUrl;
  private boolean isAvailable;
  private String productId;
}
