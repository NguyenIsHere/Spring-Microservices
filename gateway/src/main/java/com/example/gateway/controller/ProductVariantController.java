package com.example.gateway.controller;

import com.example.gateway.dto.CreateProductVariantDTO; // Bạn sẽ tạo DTO này
import com.example.gateway.dto.ProductVariantResponseDTO; // DTO trả về
import com.example.gateway.service.ProductVariantGrpcClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import product.ProductVariantResponse;
import product.ProductVariantListResponse;

@RestController
@RequestMapping("/api/v1/variants")
public class ProductVariantController {

  private static final Logger logger = LoggerFactory.getLogger(ProductVariantController.class);

  private final ProductVariantGrpcClient productVariantGrpcClient;

  public ProductVariantController(ProductVariantGrpcClient productVariantGrpcClient) {
    this.productVariantGrpcClient = productVariantGrpcClient;
  }

  /**
   * Lấy một variant theo ID
   * GET /api/test/variants/{id}
   */
  @GetMapping("/{id}")
  public ProductVariantResponseDTO getVariantById(@PathVariable String id) {
    // Gọi gRPC client để lấy variant
    ProductVariantResponse protoResponse = productVariantGrpcClient.getProductVariant(id);

    // Map sang DTO trả về JSON
    ProductVariantResponseDTO dto = new ProductVariantResponseDTO();
    dto.setId(protoResponse.getId());
    dto.setPrice(protoResponse.getPrice());
    dto.setQuantity(protoResponse.getQuantity());
    dto.setColor(protoResponse.getColor());
    dto.setSize(protoResponse.getSize());
    dto.setImageUrl(protoResponse.getImageUrl());
    dto.setAvailable(protoResponse.getIsAvailable());
    dto.setProductId(protoResponse.getProductId());
    return dto;
  }

  /**
   * Tạo một variant mới
   * POST /api/test/variants
   */
  @PostMapping
  public ProductVariantResponseDTO createVariant(@RequestBody CreateProductVariantDTO body) {

    // check isAvailable product variant
    logger.info("Received request body: {}", body);

    // Gọi gRPC client
    ProductVariantResponse protoResponse = productVariantGrpcClient.createProductVariant(
        body.getPrice(),
        body.getQuantity(),
        body.getColor(),
        body.getSize(),
        body.getImageUrl(),
        body.isAvailable(),
        body.getProductId());

    // Map sang DTO
    ProductVariantResponseDTO dto = new ProductVariantResponseDTO();
    dto.setId(protoResponse.getId());
    dto.setPrice(protoResponse.getPrice());
    dto.setQuantity(protoResponse.getQuantity());
    dto.setColor(protoResponse.getColor());
    dto.setSize(protoResponse.getSize());
    dto.setImageUrl(protoResponse.getImageUrl());
    dto.setAvailable(protoResponse.getIsAvailable());
    dto.setProductId(protoResponse.getProductId());
    return dto;
  }

  /**
   * Lấy danh sách variants theo productId
   * GET /api/test/variants/by-product/{productId}
   */
  @GetMapping("/by-product/{productId}")
  public ProductVariantListResponse getVariantsByProduct(@PathVariable String productId) {
    // Gọi gRPC client
    return productVariantGrpcClient.getVariantsByProduct(productId);
  }
}
