package com.example.graphql_gateway.resolver;

import com.example.graphql_gateway.service.ProductGrpcClient;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import org.springframework.stereotype.Component;

@Component
public class ProductResolver implements DataFetcher<Object> {

  private final ProductGrpcClient productGrpcClient;

  public ProductResolver(ProductGrpcClient productGrpcClient) {
    this.productGrpcClient = productGrpcClient;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    String productId = environment.getArgument("productId");
    return productGrpcClient.getProduct(productId);
  }
}
