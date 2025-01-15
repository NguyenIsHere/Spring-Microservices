package com.example.order_service.grpc;

import com.example.order_service.model.Order;
import com.example.order_service.model.OrderItem;
import order.PaymentRequestEvent;
import com.example.order_service.publisher.OrderEventPublisher;
import com.example.order_service.repository.OrderRepository;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import order.CreateOrderRequest;
import order.OrderResponse;
import order.UpdateOrderStatusRequest;
import websocket.WebSocketServiceGrpc;
import websocket.NotificationRequest;
import websocket.NotificationResponse;

import com.google.protobuf.Timestamp;

import cart.CartServiceGrpc;
import cart.CartResponse;
import cart.Cart;
import product.ProductResponse;
import product.ProductServiceGrpc;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class OrderGrpcService extends order.OrderServiceGrpc.OrderServiceImplBase {

  private static final Logger logger = LoggerFactory.getLogger(OrderGrpcService.class);

  @Autowired
  private OrderRepository orderRepository;

  @GrpcClient("productServiceChannel")
  private ProductServiceGrpc.ProductServiceBlockingStub productServiceStub;

  @GrpcClient("cartServiceChannel")
  private CartServiceGrpc.CartServiceBlockingStub cartServiceStub;

  @GrpcClient("webSocketGatewayChannel")
  private WebSocketServiceGrpc.WebSocketServiceBlockingStub webSocketServiceStub;

  private final OrderEventPublisher orderEventPublisher;

  public OrderGrpcService(OrderEventPublisher orderEventPublisher) {
    this.orderEventPublisher = orderEventPublisher;
  }

  private void sendNotificationToWebSocketGateway(String userId, String message) {
    try {
      NotificationRequest request = NotificationRequest.newBuilder()
          .setUserId(userId)
          .setMessage(message)
          .build();

      NotificationResponse response = webSocketServiceStub.sendNotification(request);
      if (!response.getSuccess()) {
        logger.error("Failed to send notification to WebSocket Gateway: {}", response.getError());
      } else {
        logger.info("Notification successfully sent to WebSocket Gateway for userId: {}", userId);
      }
    } catch (Exception e) {
      logger.error("Error while sending notification to WebSocket Gateway: {}", e.getMessage(), e);
    }
  }

  // @Override
  // public void createOrder(CreateOrderRequest request,
  // StreamObserver<OrderResponse> responseObserver) {
  // String userId = request.getUserId();
  // logger.info("Received createOrder request for userId: {}", userId);

  // try {
  // // Lấy thông tin giỏ hàng từ Cart Service
  // CartResponse cartResponse =
  // cartServiceStub.getCart(cart.GetCartRequest.newBuilder()
  // .setUserId(userId)
  // .build());
  // Cart cart = cartResponse.getCart();
  // logger.info("Fetched cart for userId {}: {}", userId, cart);

  // // Tạo đơn hàng
  // Order order = new Order();
  // order.setUserId(userId);
  // order.setTotalPrice(cart.getTotalPrice());
  // order.setTotalQuantity(cart.getTotalQuantity());
  // order.setStatus("PENDING");
  // order.setCreatedAt(new Date());
  // order.setUpdatedAt(new Date());

  // // Chuyển đổi CartItem thành OrderItem với tên sản phẩm
  // List<OrderItem> orderItems = cart.getItemsList().stream()
  // .map(cartItem -> {
  // OrderItem orderItem = new OrderItem();
  // orderItem.setProductVariantId(cartItem.getProductVariant().getId());

  // // Gọi ProductService để lấy tên sản phẩm
  // try {
  // ProductResponse productResponse = productServiceStub.getProduct(
  // product.ProductRequest.newBuilder()
  // .setId(cartItem.getProductVariant().getProductId())
  // .build());

  // orderItem.setName(productResponse.getName());
  // } catch (Exception e) {
  // logger.error("Failed to fetch product name for productId: {}",
  // cartItem.getProductVariant().getProductId(), e);
  // orderItem.setName("Unknown Product");
  // }

  // orderItem.setColor(cartItem.getProductVariant().getColor());
  // orderItem.setSize(cartItem.getProductVariant().getSize());
  // orderItem.setImageUrl(cartItem.getProductVariant().getImageUrl());
  // orderItem.setQuantity(cartItem.getQuantity());
  // orderItem.setPrice(cartItem.getProductVariant().getPrice());
  // orderItem.setTotalPrice(cartItem.getPrice());
  // return orderItem;
  // })
  // .collect(Collectors.toList());

  // order.setItems(orderItems);

  // // Lưu đơn hàng vào MongoDB
  // Order savedOrder = orderRepository.save(order);
  // logger.info("Order created successfully for userId: {} with orderId: {}",
  // userId, savedOrder.getId());

  // // Gửi thông báo tới WebSocket Gateway
  // sendNotificationToWebSocketGateway(userId, "Your order has been created
  // successfully!");

  // // Trả về phản hồi gRPC
  // responseObserver.onNext(buildOrderResponse(savedOrder));
  // responseObserver.onCompleted();

  // } catch (Exception e) {
  // logger.error("Error while creating order for userId: {}. Error: {}", userId,
  // e.getMessage(), e);
  // responseObserver.onError(
  // Status.INTERNAL
  // .withDescription("Error while creating order")
  // .augmentDescription(e.getMessage())
  // .asRuntimeException());
  // }
  // }

  @Override
  public void createOrder(CreateOrderRequest request, StreamObserver<OrderResponse> responseObserver) {
    String userId = request.getUserId();
    logger.info("Received createOrder request for userId: {}", userId);

    try {
      // Xử lý logic tạo đơn hàng
      Order savedOrder = processOrderCreation(userId);

      PaymentRequestEvent event = PaymentRequestEvent.newBuilder()
          .setOrderId(savedOrder.getId())
          .setUserId(savedOrder.getUserId())
          .setTotalPrice(savedOrder.getTotalPrice())
          .addAllItems(savedOrder.getItems().stream()
              .map(item -> order.OrderItem.newBuilder()
                  .setProductVariantId(item.getProductVariantId())
                  .setName(item.getName())
                  .setColor(item.getColor())
                  .setSize(item.getSize())
                  .setImageUrl(item.getImageUrl())
                  .setQuantity(item.getQuantity())
                  .setPrice(item.getPrice())
                  .setTotalPrice(item.getTotalPrice())
                  .build())
              .collect(Collectors.toList()))
          .build();

      // Gửi event qua Kafka
      orderEventPublisher.publishPaymentRequestEvent(event);

      // Gửi thông báo tới WebSocket Gateway
      sendNotificationToWebSocketGateway(userId, "Your order has been created successfully!");

      // Trả về phản hồi gRPC
      responseObserver.onNext(buildOrderResponse(savedOrder));
      responseObserver.onCompleted();

    } catch (Exception e) {
      logger.error("Error while creating order for userId: {}. Error: {}", userId, e.getMessage(), e);
      responseObserver.onError(
          Status.INTERNAL
              .withDescription("Error while creating order")
              .augmentDescription(e.getMessage())
              .asRuntimeException());
    }
  }

  public Order processOrderCreation(String userId) throws Exception {
    logger.info("Processing order creation for userId: {}", userId);

    // Lấy thông tin giỏ hàng từ Cart Service
    CartResponse cartResponse = cartServiceStub.getCart(cart.GetCartRequest.newBuilder()
        .setUserId(userId)
        .build());
    Cart cart = cartResponse.getCart();
    logger.info("Fetched cart for userId {}: {}", userId, cart);

    // Tạo đơn hàng
    Order order = new Order();
    order.setUserId(userId);
    order.setTotalPrice(cart.getTotalPrice());
    order.setTotalQuantity(cart.getTotalQuantity());
    order.setStatus("PENDING");
    order.setCreatedAt(new Date());
    order.setUpdatedAt(new Date());

    // Chuyển đổi CartItem thành OrderItem với tên sản phẩm
    List<OrderItem> orderItems = cart.getItemsList().stream()
        .map(cartItem -> {
          OrderItem orderItem = new OrderItem();
          orderItem.setProductVariantId(cartItem.getProductVariant().getId());

          // Gọi ProductService để lấy tên sản phẩm
          try {
            ProductResponse productResponse = productServiceStub.getProduct(
                product.ProductRequest.newBuilder()
                    .setId(cartItem.getProductVariant().getProductId())
                    .build());
            orderItem.setName(productResponse.getName());
          } catch (Exception e) {
            logger.error("Failed to fetch product name for productId: {}",
                cartItem.getProductVariant().getProductId(), e);
            orderItem.setName("Unknown Product");
          }

          orderItem.setColor(cartItem.getProductVariant().getColor());
          orderItem.setSize(cartItem.getProductVariant().getSize());
          orderItem.setImageUrl(cartItem.getProductVariant().getImageUrl());
          orderItem.setQuantity(cartItem.getQuantity());
          orderItem.setPrice(cartItem.getProductVariant().getPrice());
          orderItem.setTotalPrice(cartItem.getPrice());
          return orderItem;
        })
        .collect(Collectors.toList());

    order.setItems(orderItems);

    // Lưu đơn hàng vào MongoDB
    Order savedOrder = orderRepository.save(order);
    logger.info("Order created successfully for userId: {} with orderId: {}", userId, savedOrder.getId());

    return savedOrder;
  }

  @Override
  public void getOrderById(order.GetOrderRequest request, StreamObserver<order.OrderResponse> responseObserver) {
    String orderId = request.getOrderId();
    logger.info("Fetching order by ID: {}", orderId);

    try {
      // Tìm kiếm đơn hàng trong MongoDB
      Order order = orderRepository.findById(orderId)
          .orElseThrow(() -> new RuntimeException("Order not found for orderId: " + orderId));

      // Trả về phản hồi gRPC với thông tin đơn hàng
      responseObserver.onNext(buildOrderResponse(order));
      responseObserver.onCompleted();
    } catch (Exception e) {
      logger.error("Error while fetching order by ID: {}. Error: {}", orderId, e.getMessage(), e);
      responseObserver.onError(
          Status.INTERNAL
              .withDescription("Error while fetching order by ID")
              .augmentDescription(e.getMessage())
              .asRuntimeException());
    }
  }

  @Override
  public void getOrdersByUserId(order.GetOrdersByUserIdRequest request,
      StreamObserver<order.OrdersResponse> responseObserver) {
    String userId = request.getUserId();
    logger.info("Fetching orders for userId: {}", userId);

    try {
      // Tìm kiếm tất cả các đơn hàng của người dùng trong MongoDB
      List<Order> orders = orderRepository.findByUserId(userId);

      // Chuyển đổi danh sách đơn hàng sang gRPC response
      List<order.Order> grpcOrders = orders.stream()
          .map(this::buildGrpcOrder)
          .collect(Collectors.toList());

      // Trả về phản hồi gRPC với danh sách đơn hàng
      order.OrdersResponse response = order.OrdersResponse.newBuilder()
          .addAllOrders(grpcOrders)
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      logger.error("Error while fetching orders for userId: {}. Error: {}", userId, e.getMessage(), e);
      responseObserver.onError(
          Status.INTERNAL
              .withDescription("Error while fetching orders by userId")
              .augmentDescription(e.getMessage())
              .asRuntimeException());
    }
  }

  @Override
  public void updateOrderStatus(UpdateOrderStatusRequest request, StreamObserver<OrderResponse> responseObserver) {
    String orderId = request.getOrderId();
    String newStatus = request.getStatus();
    logger.info("Updating order status for orderId: {} to status: {}", orderId, newStatus);

    try {
      // Lấy đơn hàng từ MongoDB
      Order order = orderRepository.findById(orderId)
          .orElseThrow(() -> new RuntimeException("Order not found for orderId: " + orderId));

      // Cập nhật trạng thái đơn hàng
      order.setStatus(newStatus);
      order.setUpdatedAt(new Date());
      Order updatedOrder = orderRepository.save(order);
      logger.info("Order status updated to {} for orderId: {}", newStatus, orderId);

      // Nếu trạng thái là PAID, thực hiện các hành động liên quan
      if ("PAID".equalsIgnoreCase(newStatus)) {
        // Xóa giỏ hàng của người dùng
        clearCart(order.getUserId());

        // Trừ số lượng biến thể sản phẩm
        reduceProductStock(order.getItems());
      }

      // Gửi thông báo tới WebSocket Gateway
      sendNotificationToWebSocketGateway(order.getUserId(),
          String.format("Your order #%s has been updated to %s!", orderId, newStatus));

      // Trả về phản hồi gRPC
      responseObserver.onNext(buildOrderResponse(updatedOrder));
      responseObserver.onCompleted();
    } catch (Exception e) {
      logger.error("Error while updating order status for orderId: {}. Error: {}", orderId, e.getMessage(), e);
      responseObserver.onError(
          Status.INTERNAL
              .withDescription("Error while updating order status")
              .augmentDescription(e.getMessage())
              .asRuntimeException());
    }
  }

  private void clearCart(String userId) {
    try {
      cartServiceStub.clearCart(cart.ClearCartRequest.newBuilder()
          .setUserId(userId)
          .build());
      logger.info("Successfully cleared cart for userId: {}", userId);
    } catch (Exception e) {
      logger.error("Failed to clear cart for userId: {}. Error: {}", userId, e.getMessage(), e);
      throw new RuntimeException("Failed to clear cart", e);
    }
  }

  private void reduceProductStock(List<OrderItem> orderItems) {
    for (OrderItem orderItem : orderItems) {
      try {
        productServiceStub.reduceStock(product.ReduceStockRequest.newBuilder()
            .setProductVariantId(orderItem.getProductVariantId())
            .setQuantity(orderItem.getQuantity())
            .build());
        logger.info("Reduced stock for productVariantId: {} by quantity: {}",
            orderItem.getProductVariantId(), orderItem.getQuantity());
      } catch (Exception e) {
        logger.error("Failed to reduce stock for productVariantId: {}. Error: {}",
            orderItem.getProductVariantId(), e.getMessage(), e);
        throw new RuntimeException("Failed to reduce stock", e);
      }
    }
  }

  private OrderResponse buildOrderResponse(Order order) {
    return OrderResponse.newBuilder()
        .setOrder(buildGrpcOrder(order))
        .build();
  }

  private order.Order buildGrpcOrder(Order orderEntity) {
    return order.Order.newBuilder()
        .setId(orderEntity.getId())
        .setUserId(orderEntity.getUserId())
        .setTotalPrice(orderEntity.getTotalPrice())
        .setTotalQuantity(orderEntity.getTotalQuantity())
        .setStatus(orderEntity.getStatus())
        .setCreatedAt(toGrpcTimestamp(orderEntity.getCreatedAt()))
        .setUpdatedAt(toGrpcTimestamp(orderEntity.getUpdatedAt()))
        .addAllItems(orderEntity.getItems().stream()
            .map(this::buildGrpcOrderItem)
            .collect(Collectors.toList()))
        .build();
  }

  private Timestamp toGrpcTimestamp(Date date) {
    return Timestamp.newBuilder()
        .setSeconds(date.getTime() / 1000)
        .setNanos(0)
        .build();
  }

  private order.OrderItem buildGrpcOrderItem(OrderItem orderItem) {
    return order.OrderItem.newBuilder()
        .setProductVariantId(orderItem.getProductVariantId())
        .setName(orderItem.getName())
        .setColor(orderItem.getColor())
        .setSize(orderItem.getSize())
        .setImageUrl(orderItem.getImageUrl())
        .setQuantity(orderItem.getQuantity())
        .setPrice(orderItem.getPrice())
        .setTotalPrice(orderItem.getTotalPrice())
        .build();
  }
}
