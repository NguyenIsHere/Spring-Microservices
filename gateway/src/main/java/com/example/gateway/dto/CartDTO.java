package com.example.gateway.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder // Thêm annotation này để tạo builder pattern
public class CartDTO {
  private String id;
  private String userId;
  private List<CartItemDTO> items;
  private double totalPrice;
  private int totalQuantity;
}
