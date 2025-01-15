package com.example.product_service.grpc;

import product.ProductVariantServiceGrpc;
import product.ProductVariantRequest;
import product.ProductVariantResponse;
import product.CreateProductVariantRequest;
import product.ProductVariantListResponse;
import product.GetVariantsByProductRequest;

import com.example.product_service.model.Product;
import com.example.product_service.model.ProductVariant;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.repository.ProductVariantRepository;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@GrpcService
public class ProductVariantGrpcService extends ProductVariantServiceGrpc.ProductVariantServiceImplBase {

  private static final Logger logger = LoggerFactory.getLogger(ProductVariantGrpcService.class);

  @Autowired
  private ProductVariantRepository productVariantRepository;

  @Autowired
  private ProductRepository productRepository;

  /**
   * Lấy 1 variant theo ID
   */
  @Override
  public void getProductVariant(ProductVariantRequest request,
      StreamObserver<ProductVariantResponse> responseObserver) {
    try {
      Optional<ProductVariant> optionalVariant = productVariantRepository.findById(request.getId());
      if (optionalVariant.isEmpty()) {
        responseObserver.onError(
            io.grpc.Status.NOT_FOUND
                .withDescription("ProductVariant not found for ID: " + request.getId())
                .asRuntimeException());
        return;
      }

      ProductVariant variant = optionalVariant.get();
      ProductVariantResponse response = toGrpcVariantResponse(variant);
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(
          io.grpc.Status.INTERNAL
              .withDescription("Error while fetching product variant")
              .augmentDescription(e.getMessage())
              .asRuntimeException());
    }
  }

  /**
   * Tạo 1 variant mới
   */
  @Override
  public void createProductVariant(CreateProductVariantRequest request,
      StreamObserver<ProductVariantResponse> responseObserver) {
    try {

      // check isAvailable product variant
      logger.info("check isAvailable from product-service: " + request.getIsAvailable());

      Optional<Product> optionalProduct = productRepository.findById(request.getProductId());
      if (optionalProduct.isEmpty()) {
        responseObserver.onError(
            Status.NOT_FOUND.withDescription("Product not found for ID: " + request.getProductId())
                .asRuntimeException());
        return;
      }

      Product product = optionalProduct.get();

      // Tạo mới ProductVariant
      ProductVariant variant = new ProductVariant();
      variant.setPrice(Double.parseDouble(request.getPrice()));
      variant.setQuantity(request.getQuantity());
      variant.setColor(request.getColor());
      variant.setSize(request.getSize());
      variant.setImageUrl(request.getImageUrl());
      variant.setAvailable(request.getIsAvailable()); // Gán đúng giá trị isAvailable
      variant.setProductId(product.getId());

      ProductVariant savedVariant = productVariantRepository.save(variant);

      ProductVariantResponse response = ProductVariantResponse.newBuilder()
          .setId(savedVariant.getId())
          .setPrice(savedVariant.getPrice())
          .setQuantity(savedVariant.getQuantity())
          .setColor(savedVariant.getColor())
          .setSize(savedVariant.getSize())
          .setImageUrl(savedVariant.getImageUrl())
          .setIsAvailable(savedVariant.isAvailable()) // Đảm bảo giá trị đúng
          .setProductId(savedVariant.getProductId())
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(
          Status.INTERNAL.withDescription("Error while creating product variant").augmentDescription(e.getMessage())
              .asRuntimeException());
    }
  }

  /**
   * Lấy danh sách variant theo productId
   */
  @Override
  public void getVariantsByProduct(GetVariantsByProductRequest request,
      StreamObserver<ProductVariantListResponse> responseObserver) {
    try {
      Optional<Product> optionalProduct = productRepository.findById(request.getProductId());
      if (optionalProduct.isEmpty()) {
        responseObserver.onError(
            io.grpc.Status.NOT_FOUND
                .withDescription("Product not found for ID: " + request.getProductId())
                .asRuntimeException());
        return;
      }
      Product product = optionalProduct.get();

      // Lấy danh sách variants từ product
      List<ProductVariant> variantList = product.getVariants();

      List<ProductVariantResponse> variantResponses = variantList.stream()
          .map(this::toGrpcVariantResponse)
          .collect(Collectors.toList());

      ProductVariantListResponse response = ProductVariantListResponse.newBuilder()
          .addAllVariants(variantResponses)
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(
          io.grpc.Status.INTERNAL
              .withDescription("Error while fetching product variants")
              .augmentDescription(e.getMessage())
              .asRuntimeException());
    }
  }

  private ProductVariantResponse toGrpcVariantResponse(ProductVariant variant) {
    return ProductVariantResponse.newBuilder()
        .setId(variant.getId())
        .setPrice(variant.getPrice())
        .setQuantity(variant.getQuantity())
        .setColor(variant.getColor())
        .setSize(variant.getSize())
        .setImageUrl(variant.getImageUrl())
        .setIsAvailable(variant.isAvailable()) // Đảm bảo giá trị đúng
        .setProductId(variant.getProductId()) // Lấy ID của product gốc
        .build();
  }
}
