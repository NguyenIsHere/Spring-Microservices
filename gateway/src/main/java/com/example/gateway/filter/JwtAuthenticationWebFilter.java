package com.example.gateway.filter;

import auth.VerifyTokenResponse;
import com.example.gateway.service.AuthGrpcClient;
import com.example.gateway.service.UserGrpcClient;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
public class JwtAuthenticationWebFilter implements WebFilter {

  private final AuthGrpcClient authGrpcClient;
  private final UserGrpcClient userGrpcClient;

  public JwtAuthenticationWebFilter(AuthGrpcClient authGrpcClient, UserGrpcClient userGrpcClient) {
    this.authGrpcClient = authGrpcClient;
    this.userGrpcClient = userGrpcClient;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String path = exchange.getRequest().getURI().getPath();
    if (path.startsWith("/api/v1/auth/register") || path.startsWith("/api/v1/auth/login")
        || path.startsWith("/api/v1/payments/callback")) {
      return chain.filter(exchange);
    }

    String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);

      try {
        VerifyTokenResponse verifyResponse = authGrpcClient.verifyToken(token);

        if (verifyResponse.getIsValid()) {
          // Gọi User Service để lấy userId từ email
          return userGrpcClient.getUserIdByEmail(verifyResponse.getEmail())
              .flatMap(userId -> {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    verifyResponse.getEmail(),
                    null,
                    Collections.singleton(() -> verifyResponse.getRole()));
                authentication.setDetails(userId); // Lưu userId vào details

                SecurityContext context = new SecurityContextImpl(authentication);
                return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
              })
              .onErrorResume(e -> {
                System.err.println("Error fetching userId: " + e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
              });
        }
      } catch (Exception e) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
      }
    }

    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    return exchange.getResponse().setComplete();
  }
}
