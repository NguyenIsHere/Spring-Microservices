package com.example.gateway.dto;

import lombok.Data;

@Data
public class UpdateOrderStatusDTO {
  private String status; // PENDING, PAID, CANCELLED
}
