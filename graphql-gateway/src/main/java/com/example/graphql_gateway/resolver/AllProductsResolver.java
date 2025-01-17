package com.example.graphql_gateway.resolver;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.graphql_gateway.dto.ProductDTO;
import com.example.graphql_gateway.service.ProductGrpcClient;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import product.ProductListResponse;
import product.ProductResponse;

@Component
public class AllProductsResolver implements DataFetcher<List<ProductDTO>> {

  private final ProductGrpcClient productGrpcClient;

  public AllProductsResolver(ProductGrpcClient productGrpcClient) {
    this.productGrpcClient = productGrpcClient;
  }

  @Override
  public List<ProductDTO> get(DataFetchingEnvironment environment) {
    ProductListResponse response = productGrpcClient.getAllProducts();
    return response.getProductsList().stream()
        .map(this::mapToProductDTO)
        .collect(Collectors.toList());
  }

  private ProductDTO mapToProductDTO(ProductResponse response) {
    ProductDTO productDTO = new ProductDTO();
    productDTO.setProductId(response.getId());
    productDTO.setName(response.getName());
    productDTO.setDescription(response.getDescription());
    if (!response.getVariantsList().isEmpty()) {
      productDTO.setPrice(response.getVariantsList().get(0).getPrice());
    }
    return productDTO;
  }
}
