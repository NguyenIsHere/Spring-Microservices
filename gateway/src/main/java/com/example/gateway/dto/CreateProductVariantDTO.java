package com.example.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CreateProductVariantDTO {
  private String price; // Dưới dạng string, server parse double
  private int quantity;
  private String color;
  private String size;
  private String imageUrl;
  @JsonProperty("isAvailable")
  private boolean isAvailable;
  private String productId;
}
