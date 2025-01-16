package com.example.gateway.controller;

import com.example.gateway.dto.AddToCartDTO;
import com.example.gateway.service.CartGrpcClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

  private final CartGrpcClient cartGrpcClient;

  public CartController(CartGrpcClient cartGrpcClient) {
    this.cartGrpcClient = cartGrpcClient;
  }

  @PostMapping("/add")
  public Mono<ResponseEntity<String>> addToCart(@RequestBody AddToCartDTO addToCartDTO) {
    return getCurrentUserId()
        .doOnNext(userId -> addToCartDTO.setUserId(userId))
        .flatMap(userId -> {
          cartGrpcClient.addToCart(addToCartDTO);
          return Mono.just(ResponseEntity.accepted().body("Your request is being processed."));
        });
  }

  @GetMapping
  public Mono<ResponseEntity<String>> getCart() {
    return getCurrentUserId()
        .flatMap(userId -> {
          cartGrpcClient.getCart(userId);
          return Mono.just(ResponseEntity.accepted().body("Your request is being processed."));
        });
  }

  @DeleteMapping
  public Mono<ResponseEntity<String>> clearCart() {
    return getCurrentUserId()
        .flatMap(userId -> {
          cartGrpcClient.clearCart(userId);
          return Mono.just(ResponseEntity.accepted().body("Your request is being processed."));
        });
  }

  /**
   * Lấy userId từ SecurityContext.
   */
  private Mono<String> getCurrentUserId() {
    return ReactiveSecurityContextHolder.getContext()
        .map(context -> {
          Authentication authentication = context.getAuthentication();
          if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("User not authenticated");
          }
          // Lấy userId từ details
          Object userId = authentication.getDetails();
          if (userId == null || !(userId instanceof String)) {
            throw new IllegalStateException("User ID not found in authentication details");
          }
          return (String) userId;
        });
  }

}
