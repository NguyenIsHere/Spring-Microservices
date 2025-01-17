package com.example.graphql_gateway.config;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class GraphQLConfig {

  @Bean
  public GraphQL graphQL() throws Exception {
    File schemaFile = new File("src/main/resources/schema.graphqls");
    SchemaParser schemaParser = new SchemaParser();
    GraphQLSchema graphQLSchema = new SchemaGenerator()
        .makeExecutableSchema(schemaParser.parse(schemaFile), runtimeWiring());

    return GraphQL.newGraphQL(graphQLSchema).build();
  }

  private RuntimeWiring runtimeWiring() {
    return RuntimeWiring.newRuntimeWiring()
        .type("Query", typeWiring -> typeWiring
            .dataFetcher("getUser", new UserResolver())
            .dataFetcher("getProduct", new ProductResolver()))
        .build();
  }
}
