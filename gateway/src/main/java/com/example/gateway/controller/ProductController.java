package com.example.gateway.controller;

import com.example.gateway.dto.CreateProductDTO;

import org.springframework.web.bind.annotation.*;
import product.ProductResponse;
import product.ProductListResponse;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

  private final com.example.gateway.service.ProductGrpcClient productGrpcClient;

  public ProductController(com.example.gateway.service.ProductGrpcClient productGrpcClient) {
    this.productGrpcClient = productGrpcClient;
  }

  @GetMapping("/{id}")
  public ProductResponse getProduct(@PathVariable String id) {
    return productGrpcClient.getProduct(id);
  }

  @PostMapping
  public CreateProductDTO createProduct(@RequestBody CreateProductDTO dto) {
    ProductResponse protoResponse = productGrpcClient.createProduct(dto.getName(), dto.getDescription(), dto.getBrand(),
        dto.getCategory());
    // map -> DTO
    CreateProductDTO responseDTO = new CreateProductDTO();
    responseDTO.setName(protoResponse.getName());
    responseDTO.setDescription(protoResponse.getDescription());
    responseDTO.setBrand(protoResponse.getBrand());
    responseDTO.setCategory(protoResponse.getCategory());
    return responseDTO;
  }

  @GetMapping
  public ProductListResponse getAllProducts() {
    return productGrpcClient.getAllProducts();
  }
}
