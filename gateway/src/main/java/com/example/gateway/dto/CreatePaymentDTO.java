package com.example.gateway.dto;

import lombok.Data;

@Data
public class CreatePaymentDTO {
  private String orderId;
  private String callback_url;
}
