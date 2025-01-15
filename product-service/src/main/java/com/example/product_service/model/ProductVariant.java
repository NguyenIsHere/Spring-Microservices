package com.example.product_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "products_variant")
public class ProductVariant {
  @Id
  private String id;
  private double price;
  private int quantity;
  private String color;
  private String size;
  private String imageUrl;
  private boolean isAvailable;

  // Liên kết với Product
  private String productId;
}
