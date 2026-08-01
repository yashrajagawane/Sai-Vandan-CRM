package com.saivandan.crm.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import io.jsonwebtoken.Claims;

@Service
public class JwtService {
  private final SecretKey key;
  private final long accessMinutes;
  private final long refreshDays;
  public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.access-token-minutes}") long accessMinutes, @Value("${app.jwt.refresh-token-days}") long refreshDays) {
    if (secret.getBytes(StandardCharsets.UTF_8).length < 32) throw new IllegalArgumentException("JWT secret must be at least 32 bytes");
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.accessMinutes = accessMinutes; this.refreshDays = refreshDays;
  }
  public String accessToken(CurrentUser user) { return token(user.user().getId(), user.getUsername(), "access", Instant.now().plusSeconds(accessMinutes * 60)); }
  public String refreshToken(CurrentUser user) { return token(user.user().getId(), user.getUsername(), "refresh", Instant.now().plusSeconds(refreshDays * 86400)); }
  private String token(UUID id, String subject, String type, Instant expires) {
    return Jwts.builder().id(UUID.randomUUID().toString()).subject(subject).claim("userId", id.toString()).claim("type", type).issuedAt(new Date()).expiration(Date.from(expires)).signWith(key).compact();
  }
  public String subject(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject(); }
  public Claims claims(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); }
  public String tokenType(String token) { return claims(token).get("type", String.class); }
  public UUID userId(String token) { return UUID.fromString(claims(token).get("userId", String.class)); }
}
