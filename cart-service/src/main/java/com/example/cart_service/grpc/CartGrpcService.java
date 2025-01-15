package com.example.cart_service.grpc;

import com.example.cart_service.model.Cart;
import com.example.cart_service.model.CartItem;
import com.example.cart_service.repository.CartRepository;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cart.AddToCartRequest;
import cart.CartResponse;
import cart.ClearCartRequest;
import cart.GetCartRequest;
import cart.CartServiceGrpc;

import product.ProductVariant; // Import từ product.proto qua gRPC
import product.ProductVariantRequest;
import product.ProductVariantResponse;

import net.devh.boot.grpc.client.inject.GrpcClient;
import product.ProductVariantServiceGrpc;
import websocket.WebSocketServiceGrpc;
import websocket.NotificationRequest;
import websocket.NotificationResponse;

import java.util.ArrayList;
import java.util.Optional;

@GrpcService
public class CartGrpcService extends CartServiceGrpc.CartServiceImplBase {

  private static final Logger logger = LoggerFactory.getLogger(CartGrpcService.class);

  @Autowired
  private CartRepository cartRepository;

  @GrpcClient("productServiceChannel") // Tên channel được cấu hình trong application.properties
  private ProductVariantServiceGrpc.ProductVariantServiceBlockingStub productVariantServiceStub;

  // Thêm client gRPC cho WebSocket Gateway
  @GrpcClient("webSocketGatewayChannel") // Tên channel được cấu hình trong application.properties
  private WebSocketServiceGrpc.WebSocketServiceBlockingStub webSocketServiceStub;

  private void sendNotificationToWebSocketGateway(String userId, String message) {
    try {
      // Tạo NotificationRequest
      NotificationRequest request = NotificationRequest.newBuilder()
          .setUserId(userId)
          .setMessage(message)
          .build();

      // Gọi gRPC tới WebSocket Gateway
      NotificationResponse response = webSocketServiceStub.sendNotification(request);

      // Kiểm tra phản hồi từ WebSocket Gateway
      if (!response.getSuccess()) {
        logger.error("Failed to send notification to WebSocket Gateway: {}", response.getError());
      } else {
        logger.info("Notification successfully sent to WebSocket Gateway for userId: {}", userId);
      }
    } catch (Exception e) {
      logger.error("Error while sending notification to WebSocket Gateway: {}", e.getMessage(), e);
    }
  }

  /**
   * Thêm sản phẩm vào giỏ hàng.
   */
  private void ensureCartItemsInitialized(Cart cart) {
    if (cart.getItems() == null) {
      cart.setItems(new ArrayList<>());
    }
  }

  @Override
  public void addToCart(AddToCartRequest request, StreamObserver<CartResponse> responseObserver) {
    try {
      // Logic xử lý thêm sản phẩm vào giỏ hàng (giữ nguyên như trước)
      Cart cart = cartRepository.findByUserId(request.getUserId())
          .orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUserId(request.getUserId());
            return cartRepository.save(newCart);
          });

      ensureCartItemsInitialized(cart);

      Optional<CartItem> existingItem = cart.getItems().stream()
          .filter(item -> item.getProductVariant().getId().equals(request.getProductVariantId()))
          .findFirst();

      if (existingItem.isPresent()) {
        CartItem cartItem = existingItem.get();
        cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        cartItem.setPrice(cartItem.getQuantity() * cartItem.getProductVariant().getPrice());
      } else {
        ProductVariant productVariant = fetchProductVariant(request.getProductVariantId());
        CartItem newItem = new CartItem();
        newItem.setProductVariant(productVariant);
        newItem.setQuantity(request.getQuantity());
        newItem.setPrice(productVariant.getPrice() * request.getQuantity());
        cart.getItems().add(newItem);
      }

      updateCartTotals(cart);
      Cart savedCart = cartRepository.save(cart);

      // Gửi thông báo tới WebSocket Gateway
      sendNotificationToWebSocketGateway(request.getUserId(),
          "Product " + request.getProductVariantId() + " added to cart.");

      responseObserver.onNext(buildCartResponse(savedCart));
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(
          Status.INTERNAL
              .withDescription("Error while adding to cart")
              .augmentDescription(e.getMessage())
              .asRuntimeException());
    }
  }

  /**
   * Lấy giỏ hàng theo userId.
   */
  @Override
  public void getCart(GetCartRequest request, StreamObserver<CartResponse> responseObserver) {
    try {
      Optional<Cart> optionalCart = cartRepository.findByUserId(request.getUserId());
      if (optionalCart.isEmpty()) {
        responseObserver.onError(
            Status.NOT_FOUND
                .withDescription("Cart not found for userId: " + request.getUserId())
                .asRuntimeException());
        return;
      }

      responseObserver.onNext(buildCartResponse(optionalCart.get()));
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(
          Status.INTERNAL
              .withDescription("Error while fetching cart")
              .augmentDescription(e.getMessage())
              .asRuntimeException());
    }
  }

  /**
   * Xóa toàn bộ giỏ hàng của người dùng.
   */
  @Override
  public void clearCart(ClearCartRequest request, StreamObserver<CartResponse> responseObserver) {
    try {
      Optional<Cart> optionalCart = cartRepository.findByUserId(request.getUserId());
      if (optionalCart.isEmpty()) {
        responseObserver.onError(
            Status.NOT_FOUND
                .withDescription("Cart not found for userId: " + request.getUserId())
                .asRuntimeException());
        return;
      }

      Cart cart = optionalCart.get();
      cart.getItems().clear();
      cart.setTotalPrice(0.0);
      cart.setTotalQuantity(0);

      Cart updatedCart = cartRepository.save(cart);

      // Gửi thông báo tới WebSocket Gateway
      sendNotificationToWebSocketGateway(request.getUserId(), "Your cart has been cleared.");

      responseObserver.onNext(buildCartResponse(updatedCart));
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(
          Status.INTERNAL
              .withDescription("Error while clearing cart")
              .augmentDescription(e.getMessage())
              .asRuntimeException());
    }
  }

  /**
   * Fetch ProductVariant từ Product Service qua gRPC.
   */
  private ProductVariant fetchProductVariant(String productVariantId) {
    try {
      ProductVariantRequest request = ProductVariantRequest.newBuilder()
          .setId(productVariantId)
          .build();

      ProductVariantResponse response = productVariantServiceStub.getProductVariant(request);

      return ProductVariant.newBuilder()
          .setId(response.getId())
          .setPrice(response.getPrice())
          .setColor(response.getColor())
          .setSize(response.getSize())
          .setImageUrl(response.getImageUrl())
          .setIsAvailable(response.getIsAvailable())
          .setProductId(response.getProductId())
          .build();
    } catch (io.grpc.StatusRuntimeException e) {
      if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
        throw new RuntimeException("ProductVariant not found: " + productVariantId, e);
      }
      throw new RuntimeException("Error while fetching ProductVariant with ID: " + productVariantId, e);
    }
  }

  /**
   * Tính toán lại tổng giá trị và số lượng giỏ hàng.
   */
  private void updateCartTotals(Cart cart) {
    double totalPrice = cart.getItems().stream()
        .mapToDouble(CartItem::getPrice)
        .sum();
    int totalQuantity = cart.getItems().stream()
        .mapToInt(CartItem::getQuantity)
        .sum();

    cart.setTotalPrice(totalPrice);
    cart.setTotalQuantity(totalQuantity);
  }

  /**
   * Xây dựng CartResponse từ Cart.
   */
  private cart.CartResponse buildCartResponse(com.example.cart_service.model.Cart cartModel) {
    // Xây dựng Cart từ message
    cart.Cart.Builder grpcCartBuilder = cart.Cart.newBuilder()
        .setId(cartModel.getId())
        .setUserId(cartModel.getUserId())
        .setTotalPrice(cartModel.getTotalPrice())
        .setTotalQuantity(cartModel.getTotalQuantity());

    // Duyệt qua danh sách CartItem và thêm vào Cart.Builder
    cartModel.getItems().forEach(item -> grpcCartBuilder.addItems(
        cart.CartItem.newBuilder()
            .setQuantity(item.getQuantity())
            .setPrice(item.getPrice())
            .setProductVariant(
                product.ProductVariant.newBuilder()
                    .setId(item.getProductVariant().getId())
                    .setPrice(item.getProductVariant().getPrice())
                    .setColor(item.getProductVariant().getColor())
                    .setSize(item.getProductVariant().getSize())
                    .setImageUrl(item.getProductVariant().getImageUrl())
                    .setIsAvailable(item.getProductVariant().getIsAvailable())
                    .setProductId(item.getProductVariant().getProductId())
                    .build())
            .build()));

    // Trả về CartResponse
    return cart.CartResponse.newBuilder()
        .setCart(grpcCartBuilder.build())
        .build();
  }

}
