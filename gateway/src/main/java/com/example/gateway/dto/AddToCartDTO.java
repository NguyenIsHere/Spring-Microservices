package com.example.gateway.dto;

import lombok.Data;

@Data
public class AddToCartDTO {
  private String userId;
  private String productVariantId;
  private int quantity;
}
