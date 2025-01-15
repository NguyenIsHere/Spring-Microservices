package com.example.product_service.grpc;

import product.ProductServiceGrpc;
import product.ProductRequest;
import product.ProductResponse;
import product.CreateProductRequest;
import product.ProductListResponse;

import com.example.product_service.model.Product;
import com.example.product_service.model.ProductVariant;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.repository.ProductVariantRepository;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@GrpcService
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {

  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProductGrpcService.class);

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private ProductVariantRepository productVariantRepository;

  @Override
  public void getProduct(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
    try {
      Optional<Product> optionalProduct = productRepository.findById(request.getId());
      if (optionalProduct.isEmpty()) {
        responseObserver.onError(
            io.grpc.Status.NOT_FOUND
                .withDescription("Product not found for ID: " + request.getId())
                .asRuntimeException());
        return;
      }

      Product product = optionalProduct.get();

      // Lấy danh sách variant (nếu có)
      // Tùy cách bạn lưu, có thể fetch qua DocumentReference hoặc custom query
      List<product.ProductVariant> variantList = productVariantRepository
          .findAllById(product.getVariants()
              .stream()
              .map(ProductVariant::getId)
              .collect(Collectors.toList()))
          .stream()
          .map(this::toGrpcVariant)
          .collect(Collectors.toList());

      ProductResponse response = ProductResponse.newBuilder()
          .setId(product.getId())
          .setName(product.getName())
          .setDescription(product.getDescription())
          .setBrand(product.getBrand())
          .setCategory(product.getCategory())
          .addAllVariants(variantList)
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(
          io.grpc.Status.INTERNAL
              .withDescription("Error while fetching product")
              .augmentDescription(e.getMessage())
              .asRuntimeException());
    }
  }

  @Override
  public void createProduct(CreateProductRequest request, StreamObserver<ProductResponse> responseObserver) {
    try {
      Product product = new Product();
      product.setName(request.getName());
      product.setDescription(request.getDescription());
      product.setBrand(request.getBrand());
      product.setCategory(request.getCategory());

      Product savedProduct = productRepository.save(product);

      ProductResponse response = ProductResponse.newBuilder()
          .setId(savedProduct.getId())
          .setName(savedProduct.getName())
          .setDescription(savedProduct.getDescription())
          .setBrand(savedProduct.getBrand())
          .setCategory(savedProduct.getCategory())
          // Chưa gán variants => rỗng
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(
          io.grpc.Status.INTERNAL
              .withDescription("Error while creating product")
              .augmentDescription(e.getMessage())
              .asRuntimeException());
    }
  }

  @Override
  public void getAllProducts(Empty request, StreamObserver<ProductListResponse> responseObserver) {
    try {
      List<Product> products = productRepository.findAll();

      List<ProductResponse> productResponses = products.stream()
          .map(product -> ProductResponse.newBuilder()
              .setId(product.getId())
              .setName(product.getName())
              .setDescription(product.getDescription())
              .setBrand(product.getBrand())
              .setCategory(product.getCategory())
              // .addAllVariants(...) => bạn có thể fetch variant nếu muốn
              .build())
          .collect(Collectors.toList());

      ProductListResponse response = ProductListResponse.newBuilder()
          .addAllProducts(productResponses)
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(
          io.grpc.Status.INTERNAL
              .withDescription("Error while fetching all products")
              .augmentDescription(e.getMessage())
              .asRuntimeException());
    }
  }

  @Override
  public void reduceStock(product.ReduceStockRequest request, StreamObserver<Empty> responseObserver) {
    String productVariantId = request.getProductVariantId();
    int quantityToReduce = request.getQuantity();
    try {
      // Lấy ProductVariant từ MongoDB
      ProductVariant productVariant = productVariantRepository.findById(productVariantId)
          .orElseThrow(() -> new RuntimeException("ProductVariant not found for ID: " + productVariantId));

      // Kiểm tra xem có đủ số lượng để giảm hay không
      if (productVariant.getQuantity() < quantityToReduce) {
        throw new RuntimeException("Insufficient stock for ProductVariant ID: " + productVariantId);
      }

      // Giảm số lượng
      productVariant.setQuantity(productVariant.getQuantity() - quantityToReduce);

      // Cập nhật trạng thái nếu hết hàng
      if (productVariant.getQuantity() == 0) {
        productVariant.setAvailable(false);
      }

      // Lưu lại thay đổi vào MongoDB
      productVariantRepository.save(productVariant);

      // Phản hồi thành công
      responseObserver.onNext(Empty.getDefaultInstance());
      responseObserver.onCompleted();
      logger.info("Reduced stock for ProductVariant ID: {} by quantity: {}", productVariantId, quantityToReduce);
    } catch (Exception e) {
      logger.error("Error while reducing stock for ProductVariant ID: {}. Error: {}", productVariantId, e.getMessage(),
          e);
      responseObserver.onError(
          io.grpc.Status.INTERNAL
              .withDescription("Error while reducing stock")
              .augmentDescription(e.getMessage())
              .asRuntimeException());
    }
  }

  /**
   * Map từ model MongoDB => gRPC ProductVariant
   */
  private product.ProductVariant toGrpcVariant(ProductVariant variant) {
    return product.ProductVariant.newBuilder()
        .setId(variant.getId())
        .setPrice(variant.getPrice())
        .setQuantity(variant.getQuantity())
        .setColor(variant.getColor())
        .setSize(variant.getSize())
        .setImageUrl(variant.getImageUrl())
        .setIsAvailable(variant.isAvailable())
        .setProductId(variant.getProductId())
        .build();
  }
}
