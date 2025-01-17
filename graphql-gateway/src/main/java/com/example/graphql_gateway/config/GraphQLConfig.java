package com.example.graphql_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import com.example.graphql_gateway.resolver.UserResolver;

@Configuration
public class GraphQLConfig {
  private final UserResolver userResolver;

  public GraphQLConfig(UserResolver userResolver) {
    this.userResolver = userResolver;
  }

  @Bean
  public RuntimeWiringConfigurer runtimeWiringConfigurer() {
    return wiringBuilder -> wiringBuilder
        .type("Query", typeWiring -> typeWiring
            .dataFetcher("getUser", userResolver)); // Ánh xạ getUser với resolver
  }

}
