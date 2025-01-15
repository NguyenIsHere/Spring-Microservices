package com.example.cart_service.dto;

import lombok.Data;

@Data
public class AddToCartDTO {
  private String userId;
  private String productVariantId;
  private int quantity;
}
