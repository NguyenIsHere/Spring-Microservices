package com.example.product_service.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import lombok.Data;

@Data
@Document(collection = "products")
public class Product {
  @Id
  private String id;
  private String name;
  private String description;
  private String brand;
  private String category;

  // Liên kết với các ProductVariant
  @DocumentReference(lazy = true)
  private List<ProductVariant> variants;

}
