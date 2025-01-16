package com.example.gateway.config;

import com.example.gateway.filter.JwtAuthenticationWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class WebSecurityConfig {

  private final JwtAuthenticationWebFilter jwtAuthenticationWebFilter;

  public WebSecurityConfig(JwtAuthenticationWebFilter jwtAuthenticationWebFilter) {
    this.jwtAuthenticationWebFilter = jwtAuthenticationWebFilter;
  }

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http
        .csrf(csrf -> csrf.disable()) // Vô hiệu hóa CSRF
        .authorizeExchange(exchange -> exchange
            .pathMatchers("/api/v1/auth/register", "/api/v1/auth/login").permitAll() // Công khai
            .pathMatchers("/api/v1/payments/callback").permitAll() // Công khai
            .anyExchange().authenticated() // Yêu cầu xác thực
        )
        .addFilterAt(jwtAuthenticationWebFilter,
            org.springframework.security.config.web.server.SecurityWebFiltersOrder.AUTHENTICATION) // Thêm filter xác
                                                                                                   // thực
        .build();
  }
}
