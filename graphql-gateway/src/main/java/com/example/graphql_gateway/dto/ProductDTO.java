package com.example.graphql_gateway.dto;

import lombok.Data;

@Data
public class ProductDTO {
  private String productId;
  private String name;
  private String description;
  private Double price;
}
