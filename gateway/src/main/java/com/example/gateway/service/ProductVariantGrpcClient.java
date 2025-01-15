package com.example.gateway.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import product.ProductVariantServiceGrpc;
import product.ProductVariantRequest;
import product.ProductVariantResponse;
import product.CreateProductVariantRequest;
import product.ProductVariantListResponse;
import product.GetVariantsByProductRequest;

@Service
public class ProductVariantGrpcClient {

  @GrpcClient("productServiceChannel")
  private ProductVariantServiceGrpc.ProductVariantServiceBlockingStub variantStub;

  /**
   * Lấy 1 variant theo ID
   */
  public ProductVariantResponse getProductVariant(String variantId) {
    ProductVariantRequest request = ProductVariantRequest.newBuilder()
        .setId(variantId)
        .build();
    return variantStub.getProductVariant(request);
  }

  /**
   * Tạo 1 variant mới
   */
  public ProductVariantResponse createProductVariant(
      String price,
      int quantity,
      String color,
      String size,
      String imageUrl,
      boolean isAvailable,
      String productId) {
    CreateProductVariantRequest request = CreateProductVariantRequest.newBuilder()
        .setPrice(price)
        .setQuantity(quantity)
        .setColor(color)
        .setSize(size)
        .setImageUrl(imageUrl)
        .setIsAvailable(isAvailable)
        .setProductId(productId)
        .build();

    return variantStub.createProductVariant(request);
  }

  /**
   * Lấy danh sách variants theo productId
   */
  public ProductVariantListResponse getVariantsByProduct(String productId) {
    GetVariantsByProductRequest request = GetVariantsByProductRequest.newBuilder()
        .setProductId(productId)
        .build();
    return variantStub.getVariantsByProduct(request);
  }
}
