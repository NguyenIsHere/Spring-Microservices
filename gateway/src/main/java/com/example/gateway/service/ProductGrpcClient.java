package com.example.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;

import net.devh.boot.grpc.client.inject.GrpcClient;
import product.CreateProductRequest;
import product.ProductListResponse;
import product.ProductRequest;
import product.ProductResponse;
import product.ProductServiceGrpc;

@Service
public class ProductGrpcClient {

  private static final Logger logger = LoggerFactory.getLogger(ProductGrpcClient.class);
  // Annotation @GrpcClient tự động tạo channel và stub dựa trên cấu hình trong
  // application.yml
  @GrpcClient("productServiceChannel") // grpc-server là tên service trong application.yml
  private ProductServiceGrpc.ProductServiceBlockingStub productServiceStub;

  /**
   * Lấy thông tin 1 product bằng ID.
   */
  public ProductResponse getProduct(String productId) {
    ProductRequest request = ProductRequest.newBuilder()
        .setId(productId)
        .build();
    return productServiceStub.getProduct(request);
  }

  /**
   * Tạo 1 product mới, trả về thông tin product sau khi tạo.
   */
  public ProductResponse createProduct(String name, String description, String brand, String category) {
    logger.info("Attempting to create product through gRPC");
    CreateProductRequest request = CreateProductRequest.newBuilder()
        .setName(name)
        .setDescription(description)
        .setBrand(brand)
        .setCategory(category)
        .build();

    return productServiceStub.createProduct(request);
  }

  /**
   * Lấy danh sách tất cả các product.
   */
  public ProductListResponse getAllProducts() {
    // Sử dụng Empty request do .proto đã quy ước
    return productServiceStub.getAllProducts(Empty.getDefaultInstance());
  }
}
