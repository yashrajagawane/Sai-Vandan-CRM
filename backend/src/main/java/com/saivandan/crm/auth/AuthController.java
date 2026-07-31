package com.saivandan.crm.auth;

import com.saivandan.crm.security.CurrentUser;
import com.saivandan.crm.security.JwtService;
import com.saivandan.crm.user.AppUser;
import com.saivandan.crm.user.AppUserRepository;
import com.saivandan.crm.user.Role;
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
import java.util.Set;

@RestController @RequestMapping("/auth")
public class AuthController {
  private final AppUserRepository users; private final PasswordEncoder encoder; private final JwtService jwt;
  public AuthController(AppUserRepository users, PasswordEncoder encoder, JwtService jwt) { this.users = users; this.encoder = encoder; this.jwt = jwt; }
  @PostMapping("/login") public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    AppUser user = users.findByEmailIgnoreCase(request.email()).filter(AppUser::isActive).orElseThrow(() -> unauthorized());
    if (!encoder.matches(request.password(), user.getPasswordHash())) throw unauthorized();
    user.setLastLoginAt(Instant.now()); users.save(user); CurrentUser current = new CurrentUser(user);
    return ResponseEntity.ok(new AuthResponse(jwt.accessToken(current), jwt.refreshToken(current), profile(current)));
  }
  @GetMapping("/me") public UserProfile me(@AuthenticationPrincipal CurrentUser current) { return profile(current); }
  private UserProfile profile(CurrentUser current) { return new UserProfile(current.user().getId().toString(), current.user().getFullName(), current.getUsername(), current.user().getRoles().stream().map(Role::getCode).map(Enum::name).collect(java.util.stream.Collectors.toSet())); }
  private ResponseStatusException unauthorized() { return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."); }
  public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
  public record AuthResponse(String accessToken, String refreshToken, UserProfile user) {}
  public record UserProfile(String id, String fullName, String email, Set<String> roles) {}
}
