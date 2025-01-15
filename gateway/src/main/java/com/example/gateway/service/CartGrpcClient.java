package com.example.gateway.service;

import com.example.gateway.dto.AddToCartDTO;
import net.devh.boot.grpc.client.inject.GrpcClient;
import cart.AddToCartRequest;
import cart.ClearCartRequest;
import cart.GetCartRequest;
import cart.CartServiceGrpc;
import org.springframework.stereotype.Service;

@Service
public class CartGrpcClient {

  @GrpcClient("cartServiceChannel")
  private CartServiceGrpc.CartServiceBlockingStub cartServiceStub;

  /**
   * Gửi yêu cầu thêm sản phẩm vào giỏ hàng qua gRPC.
   * Phương thức này không trả về thông tin giỏ hàng mà chỉ xác nhận yêu cầu đã
   * gửi thành công.
   */
  public void addToCart(AddToCartDTO addToCartDTO) {
    AddToCartRequest request = AddToCartRequest.newBuilder()
        .setUserId(addToCartDTO.getUserId())
        .setProductVariantId(addToCartDTO.getProductVariantId())
        .setQuantity(addToCartDTO.getQuantity())
        .build();

    // Gửi yêu cầu tới Cart Service mà không chờ kết quả trả về
    cartServiceStub.addToCart(request);
  }

  /**
   * Gửi yêu cầu lấy giỏ hàng qua gRPC (chỉ giữ lại nếu bạn cần sử dụng ở nơi
   * khác).
   */
  public void getCart(String userId) {
    GetCartRequest request = GetCartRequest.newBuilder()
        .setUserId(userId)
        .build();

    // Gửi yêu cầu tới Cart Service (không cần xử lý kết quả tại đây)
    cartServiceStub.getCart(request);
  }

  /**
   * Gửi yêu cầu xóa giỏ hàng qua gRPC.
   */
  public void clearCart(String userId) {
    ClearCartRequest request = ClearCartRequest.newBuilder()
        .setUserId(userId)
        .build();

    // Gửi yêu cầu tới Cart Service (không cần xử lý kết quả tại đây)
    cartServiceStub.clearCart(request);
  }
}
