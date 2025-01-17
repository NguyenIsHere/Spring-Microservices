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

import java.io.InputStreamReader;

@Configuration
public class GraphQLConfig {

  private final UserResolver userResolver;
  private final ProductResolver productResolver;
  private final ResourceLoader resourceLoader;

  public GraphQLConfig(UserResolver userResolver, ProductResolver productResolver, ResourceLoader resourceLoader) {
    this.userResolver = userResolver;
    this.productResolver = productResolver;
    this.resourceLoader = resourceLoader;
  }

  @Bean
  GraphQL graphQL() throws Exception {
    Resource resource = resourceLoader.getResource("classpath:schema.graphqls");
    SchemaParser schemaParser = new SchemaParser();
    GraphQLSchema graphQLSchema = new SchemaGenerator()
        .makeExecutableSchema(
            schemaParser.parse(new InputStreamReader(resource.getInputStream())),
            runtimeWiring());

    return GraphQL.newGraphQL(graphQLSchema).build();
  }

  private RuntimeWiring runtimeWiring() {
    return RuntimeWiring.newRuntimeWiring()
        .type("Query", typeWiring -> typeWiring
            .dataFetcher("getUser", userResolver) // Sử dụng resolver được quản lý bởi Spring
            .dataFetcher("getProduct", productResolver))
        .build();
  }
}
