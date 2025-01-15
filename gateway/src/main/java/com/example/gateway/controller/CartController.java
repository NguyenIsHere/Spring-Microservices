package com.example.gateway.controller;

import com.example.gateway.dto.AddToCartDTO;
import com.example.gateway.service.CartGrpcClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

  private final CartGrpcClient cartGrpcClient;

  public CartController(CartGrpcClient cartGrpcClient) {
    this.cartGrpcClient = cartGrpcClient;
  }

  /**
   * Gửi yêu cầu thêm sản phẩm vào giỏ hàng.
   * Trả về thông báo tạm thời: "Request is being processed."
   */
  @PostMapping("/add")
  public ResponseEntity<String> addToCart(@RequestBody AddToCartDTO addToCartDTO) {
    // Gửi yêu cầu tới Cart Service qua gRPC
    cartGrpcClient.addToCart(addToCartDTO);

    // Trả về phản hồi tạm thời
    return ResponseEntity.accepted().body("Your request is being processed.");
  }

  /**
   * Gửi yêu cầu lấy giỏ hàng.
   * Trả về thông báo tạm thời: "Request is being processed."
   */
  @GetMapping("/{userId}")
  public ResponseEntity<String> getCart(@PathVariable String userId) {
    // Gửi yêu cầu tới Cart Service qua gRPC
    cartGrpcClient.getCart(userId);

    // Trả về phản hồi tạm thời
    return ResponseEntity.accepted().body("Your request is being processed.");
  }

  /**
   * Gửi yêu cầu xóa giỏ hàng.
   * Trả về thông báo tạm thời: "Request is being processed."
   */
  @DeleteMapping("/{userId}")
  public ResponseEntity<String> clearCart(@PathVariable String userId) {
    // Gửi yêu cầu tới Cart Service qua gRPC
    cartGrpcClient.clearCart(userId);

    // Trả về phản hồi tạm thời
    return ResponseEntity.accepted().body("Your request is being processed.");
  }
}
