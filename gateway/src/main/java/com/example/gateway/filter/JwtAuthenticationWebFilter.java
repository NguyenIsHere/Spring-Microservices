package com.example.gateway.filter;

import auth.VerifyTokenResponse;
import com.example.gateway.service.AuthGrpcClient;
import com.example.gateway.service.UserGrpcClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationWebFilter.class);

  private final AuthGrpcClient authGrpcClient;
  private final UserGrpcClient userGrpcClient;

  public JwtAuthenticationWebFilter(AuthGrpcClient authGrpcClient, UserGrpcClient userGrpcClient) {
    this.authGrpcClient = authGrpcClient;
    this.userGrpcClient = userGrpcClient;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String path = exchange.getRequest().getURI().getPath();
    logger.debug("Processing request path: {}", path);

    if (path.startsWith("/api/v1/auth/register") || path.startsWith("/api/v1/auth/login")
        || path.startsWith("/api/v1/payments/callback")) {
      logger.debug("Public endpoint accessed, skipping authentication: {}", path);
      return chain.filter(exchange);
    }

    String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      logger.debug("Authorization token found: {}", token);

      try {
        VerifyTokenResponse verifyResponse = authGrpcClient.verifyToken(token);
        logger.debug("Token verified: isValid={}, email={}, role={}",
            verifyResponse.getIsValid(),
            verifyResponse.getEmail(),
            verifyResponse.getRole());

        if (verifyResponse.getIsValid()) {
          return userGrpcClient.getUserByEmail(verifyResponse.getEmail())
              .flatMap(userResponse -> {
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    verifyResponse.getEmail(),
                    null,
                    Collections.singleton(() -> "ROLE_" + verifyResponse.getRole()));
                ((UsernamePasswordAuthenticationToken) authentication).setDetails(userResponse.getUserId());

                SecurityContext context = new SecurityContextImpl(authentication);
                logger.debug("Authentication successful for user: {} with role: {}",
                    verifyResponse.getEmail(),
                    verifyResponse.getRole());

                return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
              })
              .onErrorResume(e -> {
                logger.error("Error fetching userId for email {}: {}", verifyResponse.getEmail(), e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
              });
        } else {
          logger.warn("Invalid token for request: {}", token);
        }
      } catch (Exception e) {
        logger.error("Error during token verification: {}", e.getMessage());
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
      }
    } else {
      logger.warn("No Authorization header or invalid format in request.");
    }

    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    logger.debug("Unauthorized request, returning 401.");
    return exchange.getResponse().setComplete();
  }
}