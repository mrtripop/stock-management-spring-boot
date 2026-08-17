package com.mrtripop.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Simple in-memory rate limiting filter for authentication endpoints.
 * Uses a sliding window algorithm with per-IP tracking.
 * <p>
 * Rate limits:
 * <ul>
 *   <li>/api/v1/auth/login — 10 requests per minute per IP</li>
 *   <li>/api/v1/auth/register — 3 requests per minute per IP</li>
 *   <li>/api/v1/auth/verify-* — 5 requests per minute per IP</li>
 * </ul>
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

  private static final int LOGIN_LIMIT = 10;
  private static final int REGISTER_LIMIT = 3;
  private static final int VERIFY_LIMIT = 5;
  private static final long WINDOW_MINUTES = 1;

  private final Map<String, RateLimitEntry> loginAttempts = new ConcurrentHashMap<>();
  private final Map<String, RateLimitEntry> registerAttempts = new ConcurrentHashMap<>();
  private final Map<String, RateLimitEntry> verifyAttempts = new ConcurrentHashMap<>();

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String path = request.getRequestURI();
    String clientIp = getClientIp(request);

    if (isLoginEndpoint(path) && isRateLimited(loginAttempts, clientIp, LOGIN_LIMIT)) {
      sendRateLimitResponse(response, path);
      return;
    }

    if (isRegisterEndpoint(path) && isRateLimited(registerAttempts, clientIp, REGISTER_LIMIT)) {
      sendRateLimitResponse(response, path);
      return;
    }

    if (isVerifyEndpoint(path) && isRateLimited(verifyAttempts, clientIp, VERIFY_LIMIT)) {
      sendRateLimitResponse(response, path);
      return;
    }

    filterChain.doFilter(request, response);
  }

  private boolean isLoginEndpoint(String path) {
    return "/api/v1/auth/login".equals(path);
  }

  private boolean isRegisterEndpoint(String path) {
    return "/api/v1/auth/register".equals(path);
  }

  private boolean isVerifyEndpoint(String path) {
    return path.startsWith("/api/v1/auth/verify-");
  }

  private boolean isRateLimited(Map<String, RateLimitEntry> attempts, String clientIp, int limit) {
    Instant now = Instant.now();
    RateLimitEntry entry = attempts.compute(clientIp, (key, existing) -> {
      if (existing == null || now.isAfter(existing.windowEnd)) {
        return new RateLimitEntry(1, now.plus(WINDOW_MINUTES, ChronoUnit.MINUTES));
      }
      return new RateLimitEntry(existing.count + 1, existing.windowEnd);
    });

    if (entry.count > limit) {
      log.warn("Rate limit exceeded for IP {}: {} requests in {} minute window", clientIp, entry.count, WINDOW_MINUTES);
      return true;
    }
    return false;
  }

  private String getClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  private void sendRateLimitResponse(HttpServletResponse response, String path) throws IOException {
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setContentType("application/json");
    response.getWriter().write(
        "{\"code\":\"429\",\"message\":\"Too many requests. Please try again later.\"}");
  }

  private record RateLimitEntry(int count, Instant windowEnd) {}
}