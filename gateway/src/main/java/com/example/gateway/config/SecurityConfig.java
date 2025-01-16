// package com.example.gateway.config;

// import com.example.gateway.filter.JwtAuthenticationFilter;
// import com.example.gateway.service.AuthGrpcClient;

// import java.util.List;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import
// org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import
// org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.web.SecurityFilterChain;
// import
// org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
// import org.springframework.security.web.context.SecurityContextHolderFilter;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.CorsConfigurationSource;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// @Configuration
// @EnableWebSecurity
// public class SecurityConfig {

// private final AuthGrpcClient authGrpcClient;

// public SecurityConfig(AuthGrpcClient authGrpcClient) {
// this.authGrpcClient = authGrpcClient;
// }

// @Bean
// SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

// http
// .csrf(csrf -> csrf.disable()) // Vô hiệu hóa CSRF (nếu không cần)
// .sessionManagement(session ->
// session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless
// .authorizeHttpRequests(auth -> auth
// .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login").permitAll()
// // Cho phép công khai
// .requestMatchers("/api/v1/payments/callback").permitAll() // Cho phép công
// khai
// .anyRequest().authenticated() // Các yêu cầu khác phải xác thực
// )
// .addFilterBefore(new JwtAuthenticationFilter(authGrpcClient),
// SecurityContextHolderFilter.class); // Thêm filter

// System.out.println("Applying SecurityConfig with CSRF disabled.");

// return http.build();
// }

// @Bean
// CorsConfigurationSource corsConfigurationSource() {
// CorsConfiguration configuration = new CorsConfiguration();
// configuration.setAllowedOrigins(List.of("*")); // Hoặc chỉ định các domain cụ
// thể
// configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE",
// "OPTIONS"));
// configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
// configuration.setExposedHeaders(List.of("Authorization"));
// UrlBasedCorsConfigurationSource source = new
// UrlBasedCorsConfigurationSource();
// source.registerCorsConfiguration("/**", configuration);
// return source;
// }

// }
