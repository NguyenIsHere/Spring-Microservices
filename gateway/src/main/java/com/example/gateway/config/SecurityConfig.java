// package com.example.gateway.config;

// import com.example.gateway.filter.JwtAuthenticationFilter;

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
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.CorsConfigurationSource;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// @Configuration
// @EnableWebSecurity
// public class SecurityConfig {
// @Bean
// SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
// // http.sessionManagement(management ->
// // management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
// // .authorizeHttpRequests(Authorize -> Authorize
// // .anyRequest().permitAll())
// // .addFilterBefore(new JwtAuthenticationFilter(),
// // BasicAuthenticationFilter.class)
// // .csrf(csrt -> csrt.disable());

// http
// .csrf(csrf -> csrf.disable());

// return http.build();
// }

// @Bean
// CorsConfigurationSource corsConfigurationSource() {
// CorsConfiguration configuration = new CorsConfiguration();
// configuration.setAllowedOrigins(List.of("*")); // Thay * bằng domain cụ thể
// nếu cần
// configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE",
// "OPTIONS"));
// configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
// UrlBasedCorsConfigurationSource source = new
// UrlBasedCorsConfigurationSource();
// source.registerCorsConfiguration("/**", configuration);
// return source;
// }

// }
