package com.example.graphql_gateway.resolver;

import com.example.graphql_gateway.dto.ProductDTO;
import com.example.graphql_gateway.service.ProductGrpcClient;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import product.ProductResponse;

import org.springframework.stereotype.Component;

@Component
public class ProductResolver implements DataFetcher<ProductDTO> {

  private final ProductGrpcClient productGrpcClient;

  public ProductResolver(ProductGrpcClient productGrpcClient) {
    this.productGrpcClient = productGrpcClient;
  }

  @Override
  public ProductDTO get(DataFetchingEnvironment environment) {
    String productId = environment.getArgument("productId");
    ProductResponse response = productGrpcClient.getProduct(productId);
    return mapToProductDTO(response);
  }

  private ProductDTO mapToProductDTO(ProductResponse response) {
    ProductDTO productDTO = new ProductDTO();
    productDTO.setProductId(response.getId());
    productDTO.setName(response.getName());
    productDTO.setDescription(response.getDescription());
    // Nếu muốn xử lý trường `price`, lấy từ `variants`
    if (!response.getVariantsList().isEmpty()) {
      productDTO.setPrice(response.getVariantsList().get(0).getPrice());
    }
    return productDTO;
  }
}
