package com.example.graphql_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import com.example.graphql_gateway.resolver.OrderResolver;
import com.example.graphql_gateway.resolver.UserResolver;

@Configuration
public class GraphQLConfig {
  private final UserResolver userResolver;

  public GraphQLConfig(UserResolver userResolver) {
    this.userResolver = userResolver;
  }

  @Bean
  public RuntimeWiringConfigurer runtimeWiringConfigurer(OrderResolver orderResolver) {
    return wiringBuilder -> wiringBuilder
        .type("Query", typeWiring -> typeWiring
            .dataFetcher("getUser", userResolver)
            .dataFetcher("getOrderById", env -> orderResolver.getOrderById(env.getArgument("orderId")))
            .dataFetcher("getOrdersByUserId", env -> orderResolver.getOrdersByUserId(env.getArgument("userId"))))
        .type("User", typeWiring -> typeWiring
            .dataFetcher("orders", env -> userResolver.getOrders(env.getSource())));
  }

}
