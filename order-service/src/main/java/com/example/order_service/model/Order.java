package com.example.order_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "orders")
public class Order {
  @Id
  private String id;

  private String userId; // ID hoặc email của người dùng đặt hàng
  private List<OrderItem> items; // Danh sách sản phẩm trong đơn hàng

  private double totalPrice; // Tổng giá trị đơn hàng
  private int totalQuantity; // Tổng số lượng sản phẩm trong đơn hàng
  private String status; // Trạng thái của đơn hàng: PENDING, PAID, CANCELLED

  private Date createdAt; // Ngày tạo đơn hàng
  private Date updatedAt; // Ngày cập nhật đơn hàng
}
