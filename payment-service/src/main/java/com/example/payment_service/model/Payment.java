package com.example.payment_service.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "payments")
public class Payment {
  @Id
  private String id; // ID của thanh toán (có thể sinh tự động hoặc lấy từ ZaloPay)

  private String orderId; // Liên kết tới đơn hàng
  private String userId; // ID người dùng thực hiện thanh toán

  private String status; // Trạng thái thanh toán (PENDING, PAID, FAILED, CANCELLED)
  private double amount; // Số tiền cần thanh toán
  private Date createdAt; // Thời gian tạo giao dịch
  private Date updatedAt; // Thời gian cập nhật trạng thái
}
