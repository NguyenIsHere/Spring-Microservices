package com.example.gateway.filter;

import auth.AuthServiceGrpc;
import auth.VerifyTokenRequest;
import auth.VerifyTokenResponse;
import io.grpc.StatusRuntimeException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import net.devh.boot.grpc.client.inject.GrpcClient;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @GrpcClient("authServiceChannel")
  private AuthServiceGrpc.AuthServiceBlockingStub authServiceStub;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);

      try {
        // Gọi Auth Service qua gRPC để xác thực token
        VerifyTokenResponse verifyResponse = authServiceStub.verifyToken(
            VerifyTokenRequest.newBuilder().setToken(token).build());

        if (verifyResponse.getIsValid()) {
          String email = verifyResponse.getEmail();
          String role = verifyResponse.getRole();

          UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
              email, null, Collections.singleton(() -> role));

          SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          return;
        }
      } catch (StatusRuntimeException e) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
    }

    filterChain.doFilter(request, response);
  }
}
