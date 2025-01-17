package com.example.graphql_gateway.config;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.example.graphql_gateway.resolver.ProductResolver;
import com.example.graphql_gateway.resolver.UserResolver;
import com.example.graphql_gateway.resolver.AllProductsResolver;

import java.io.InputStreamReader;

@Configuration
public class GraphQLConfig {

  private final UserResolver userResolver;
  private final ProductResolver productResolver;
  private final AllProductsResolver allProductsResolver;
  private final ResourceLoader resourceLoader;

  public GraphQLConfig(UserResolver userResolver,
      ProductResolver productResolver,
      AllProductsResolver allProductsResolver,
      ResourceLoader resourceLoader) {
    this.userResolver = userResolver;
    this.productResolver = productResolver;
    this.allProductsResolver = allProductsResolver;
    this.resourceLoader = resourceLoader;
  }

  @Bean
  public GraphQL graphQL() throws Exception {
    Resource resource = resourceLoader.getResource("classpath:schema.graphqls");

    // Parse và tạo GraphQL schema từ file schema.graphqls
    GraphQLSchema graphQLSchema = new SchemaGenerator()
        .makeExecutableSchema(
            new SchemaParser().parse(new InputStreamReader(resource.getInputStream())),
            runtimeWiring());

    return GraphQL.newGraphQL(graphQLSchema).build();
  }

  private RuntimeWiring runtimeWiring() {
    return RuntimeWiring.newRuntimeWiring()
        .type("Query", typeWiring -> typeWiring
            .dataFetcher("getUser", userResolver) // Gắn UserResolver cho getUser
            .dataFetcher("getProduct", productResolver) // Gắn ProductResolver cho getProduct
            .dataFetcher("getAllProducts", allProductsResolver)) // Gắn AllProductsResolver cho getAllProducts
        .build();
  }
}
