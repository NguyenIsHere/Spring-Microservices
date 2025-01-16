package com.example.gateway.filter;

import auth.VerifyTokenResponse;
import com.example.gateway.service.AuthGrpcClient;
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

  public JwtAuthenticationWebFilter(AuthGrpcClient authGrpcClient) {
    this.authGrpcClient = authGrpcClient;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String path = exchange.getRequest().getURI().getPath();
    if (path.startsWith("/api/v1/auth/register") || path.startsWith("/api/v1/auth/login")
        || path.startsWith("/api/v1/payments/callback")) {
      // Bỏ qua xác thực cho các endpoint công khai
      return chain.filter(exchange);
    }

    String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);

      try {
        // Gọi Auth Service để xác thực token
        VerifyTokenResponse verifyResponse = authGrpcClient.verifyToken(token);

        if (verifyResponse.getIsValid()) {
          // Tạo đối tượng xác thực
          Authentication authentication = new UsernamePasswordAuthenticationToken(
              verifyResponse.getEmail(),
              null,
              Collections.singleton(() -> verifyResponse.getRole()));

          // Đặt SecurityContext
          SecurityContext context = new SecurityContextImpl(authentication);

          // Truyền tiếp luồng với SecurityContext
          return chain.filter(exchange)
              .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
        }
      } catch (Exception e) {
        // Token không hợp lệ
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
      }
    }

    // Không có token hoặc token không hợp lệ
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    return exchange.getResponse().setComplete();
  }
}
