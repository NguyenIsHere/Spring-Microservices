package com.example.graphql_gateway.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import graphql.GraphQL;

@RestController
public class GraphQLTestController {

  private final GraphQL graphQL;

  public GraphQLTestController(GraphQL graphQL) {
    this.graphQL = graphQL;
  }

  @PostMapping("/graphql")
  public Map<String, Object> execute(@RequestBody Map<String, Object> request) {
    String query = (String) request.get("query");
    return graphQL.execute(query).toSpecification();
  }
}
