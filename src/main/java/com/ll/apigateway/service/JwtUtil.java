package com.ll.apigateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  private final SecretKey secretKey;
  private final long accessTokenExpirationSeconds;

  public JwtUtil(
      @Value("${custom.jwt.secretKey}") String secretKey,
      @Value("${custom.accessToken.expirationSeconds}") long accessTokenExpirationSeconds) {
    this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
  }

  public String generateToken(Map<String, Object> claims) {
    long now = System.currentTimeMillis();
    Date expiryDate = new Date(now + accessTokenExpirationSeconds * 1000);

    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(new Date(now))
        .setExpiration(expiryDate)
        .signWith(secretKey, SignatureAlgorithm.HS512)
        .compact();
  }

  public Map<String, Object> parseToken(String token) {
    try {
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(secretKey)
          .build()
          .parseClaimsJws(token)
          .getBody();

      return new HashMap<>(claims);
    } catch (ExpiredJwtException e) {
      throw new RuntimeException("토큰이 만료되었습니다.");
    } catch (JwtException e) {
      throw new RuntimeException("유효하지 않은 토큰입니다.");
    }
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(secretKey)
          .build()
          .parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
}