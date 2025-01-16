package com.example.auth_service.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
public class JwtProvider {

  private SecretKey secretKey = Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());

  public String generatedToken(Authentication authentication) {
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

    String role = populateAuthorities(authorities);

    String jwt = Jwts.builder()
        .claim("email", authentication.getName())
        .claim("authorities", role)
        .signWith(secretKey)
        .compact();
    return jwt;
  }

  private String populateAuthorities(Collection<? extends GrantedAuthority> authorities) {
    Set<String> auths = new HashSet<>();
    for (GrantedAuthority authority : authorities) {
      auths.add(authority.getAuthority());
    }

    return String.join(",", auths);
  }

  public String getEmailFromJwt(String jwt) {
    Claims claims = Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(jwt)
        .getPayload();

    String email = String.valueOf(claims.get("email"));
    System.out.println("Extracted email: " + email);
    return email;
  }

  public String getRoleFromJwt(String jwt) {
    Claims claims = Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(jwt)
        .getPayload();

    return String.valueOf(claims.get("authorities"));
  }

}