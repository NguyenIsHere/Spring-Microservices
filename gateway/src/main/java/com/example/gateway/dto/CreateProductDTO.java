package com.example.gateway.dto;

import lombok.Data;

@Data
public class CreateProductDTO {
  private String name;
  private String description;
  private String brand;
  private String category;
}
