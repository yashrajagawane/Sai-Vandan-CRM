package com.saivandan.crm.auth;

import com.saivandan.crm.security.CurrentUser;
import com.saivandan.crm.security.JwtService;
import com.saivandan.crm.user.AppUser;
import com.saivandan.crm.user.AppUserRepository;
import com.saivandan.crm.user.Role;
import com.saivandan.crm.security.AuditService;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Set;

@RestController @RequestMapping("/auth")
public class AuthController {
  private final AppUserRepository users; private final PasswordEncoder encoder; private final JwtService jwt; private final JdbcTemplate jdbc; private final AuditService audit;
  public AuthController(AppUserRepository users, PasswordEncoder encoder, JwtService jwt, JdbcTemplate jdbc, AuditService audit) { this.users = users; this.encoder = encoder; this.jwt = jwt; this.jdbc = jdbc; this.audit = audit; }
  @PostMapping("/login") public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, jakarta.servlet.http.HttpServletRequest http) {
    String email = request.email().toLowerCase();
    Integer recentFailures = jdbc.queryForObject("select count(*) from login_attempts where lower(email)=lower(?) and successful=false and attempted_at > ?", Integer.class, email, Instant.now().minusSeconds(900));
    if (recentFailures != null && recentFailures >= 5) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many failed attempts. Try again in 15 minutes.");
    AppUser user = users.findByEmailIgnoreCase(email).filter(AppUser::isActive).orElse(null);
    if (user == null || !encoder.matches(request.password(), user.getPasswordHash())) { jdbc.update("insert into login_attempts(email,ip_address,successful) values (?,?,false)", email, http.getRemoteAddr()); throw unauthorized(); }
    user.setLastLoginAt(Instant.now()); users.save(user); CurrentUser current = new CurrentUser(user);
    String refresh = jwt.refreshToken(current); jdbc.update("insert into refresh_tokens(user_id,token_hash,expires_at) values (?,?,?)", user.getId(), hash(refresh), Instant.now().plusSeconds(14 * 86400));
    jdbc.update("insert into user_sessions(user_id,device_label,ip_address) values (?,?,?)", user.getId(), http.getHeader("User-Agent"), http.getRemoteAddr());
    jdbc.update("insert into login_attempts(email,ip_address,successful) values (?,?,true)", email, http.getRemoteAddr());
    audit.record(user.getId(), "AUTH", user.getId(), "LOGIN", null, "success", http.getRemoteAddr());
    return ResponseEntity.ok(new AuthResponse(jwt.accessToken(current), refresh, profile(current)));
  }
  @PostMapping("/refresh") public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request, jakarta.servlet.http.HttpServletRequest http) {
    try {
      if (!"refresh".equals(jwt.tokenType(request.refreshToken()))) throw unauthorized();
      String oldHash = hash(request.refreshToken()); UUID userId = jwt.userId(request.refreshToken());
      Integer valid = jdbc.queryForObject("select count(*) from refresh_tokens where user_id=? and token_hash=? and revoked_at is null and expires_at>current_timestamp", Integer.class, userId, oldHash);
      if (valid == null || valid == 0) throw unauthorized();
      AppUser user = users.findById(userId).filter(AppUser::isActive).orElseThrow(this::unauthorized); jdbc.update("update refresh_tokens set revoked_at=current_timestamp where token_hash=?", oldHash);
      CurrentUser current = new CurrentUser(user); String next = jwt.refreshToken(current); jdbc.update("insert into refresh_tokens(user_id,token_hash,expires_at) values (?,?,?)", userId, hash(next), Instant.now().plusSeconds(14 * 86400));
      return ResponseEntity.ok(new AuthResponse(jwt.accessToken(current), next, profile(current)));
    } catch (ResponseStatusException ex) { throw ex; }
    catch (Exception ex) { throw unauthorized(); }
  }
  @PostMapping("/logout") public void logout(@RequestBody(required=false) RefreshRequest request, @AuthenticationPrincipal CurrentUser current, jakarta.servlet.http.HttpServletRequest http) {
    if (request != null && request.refreshToken() != null) jdbc.update("update refresh_tokens set revoked_at=current_timestamp where token_hash=?", hash(request.refreshToken()));
    if (current != null) { jdbc.update("update refresh_tokens set revoked_at=current_timestamp where user_id=? and revoked_at is null", current.user().getId()); jdbc.update("update user_sessions set revoked_at=current_timestamp where user_id=? and revoked_at is null", current.user().getId()); audit.record(current.user().getId(), "AUTH", current.user().getId(), "LOGOUT", null, "success", http.getRemoteAddr()); }
  }
  @GetMapping("/me") public UserProfile me(@AuthenticationPrincipal CurrentUser current) { return profile(current); }
  private UserProfile profile(CurrentUser current) { return new UserProfile(current.user().getId().toString(), current.user().getFullName(), current.getUsername(), current.user().getRoles().stream().map(Role::getCode).map(Enum::name).collect(java.util.stream.Collectors.toSet())); }
  private ResponseStatusException unauthorized() { return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."); }
  private String hash(String value) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); } catch (Exception ex) { throw new IllegalStateException(ex); } }
  public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
  public record RefreshRequest(@NotBlank String refreshToken) {}
  public record AuthResponse(String accessToken, String refreshToken, UserProfile user) {}
  public record UserProfile(String id, String fullName, String email, Set<String> roles) {}
}
