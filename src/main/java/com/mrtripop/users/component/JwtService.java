package com.mrtripop.users.component;

import com.mrtripop.config.AuthProperties;
import com.mrtripop.exception.ApplicationException;
import com.mrtripop.users.constant.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtService {
  private final AuthProperties authProperties;
  private SecretKey signingKey;

  @PostConstruct
  void init() {
    this.signingKey = Keys.hmacShaKeyFor(authProperties.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(UUID userId, String username, Object role, UUID storeId) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + authProperties.getAccessTokenExpiration());
    return Jwts.builder()
        .subject(userId.toString())
        .claim("username", username)
        .claim("role", role.toString())
        .claim("storeId", storeId != null ? storeId.toString() : null)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(signingKey)
        .compact();
  }

  public String generateTempToken(UUID userId) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + authProperties.getTempTokenExpiration());
    return Jwts.builder()
        .subject(userId.toString())
        .claim("type", "TEMP")
        .issuedAt(now)
        .expiration(expiry)
        .signWith(signingKey)
        .compact();
  }

  public Claims extractClaims(String token) throws ApplicationException {
    try {
      return Jwts.parser()
          .verifyWith(signingKey)
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (ExpiredJwtException e) {
      throw new ApplicationException(ErrorCode.AUTH_TOKEN_EXPIRED, org.springframework.http.HttpStatus.UNAUTHORIZED);
    } catch (MalformedJwtException | SignatureException | IllegalArgumentException e) {
      throw new ApplicationException(ErrorCode.AUTH_TOKEN_INVALID, org.springframework.http.HttpStatus.UNAUTHORIZED);
    }
  }

  public UUID extractUserId(String token) throws ApplicationException {
    return UUID.fromString(extractClaims(token).getSubject());
  }

  public boolean isTempToken(String token) {
    try {
      Claims claims = extractClaims(token);
      return "TEMP".equals(claims.get("type", String.class));
    } catch (ApplicationException e) {
      return false;
    }
  }

  public boolean validateToken(String token) {
    try {
      extractClaims(token);
      return true;
    } catch (ApplicationException e) {
      return false;
    }
  }

  public boolean isAccessToken(String token) {
    try {
      Claims claims = extractClaims(token);
      return !"TEMP".equals(claims.get("type", String.class));
    } catch (ApplicationException e) {
      return false;
    }
  }

  public long getAccessTokenExpiration() {
    return authProperties.getAccessTokenExpiration();
  }
}