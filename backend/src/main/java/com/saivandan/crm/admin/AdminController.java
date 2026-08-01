package com.saivandan.crm.admin;

import com.saivandan.crm.security.AuditService;
import com.saivandan.crm.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController {
  private final JdbcTemplate jdbc; private final PasswordEncoder encoder; private final AuditService audit;
  public AdminController(JdbcTemplate jdbc, PasswordEncoder encoder, AuditService audit) { this.jdbc = jdbc; this.encoder = encoder; this.audit = audit; }

  @GetMapping("/users")
  public List<Map<String,Object>> users() {
    return jdbc.queryForList("select u.id,u.full_name as fullName,u.email,u.mobile,u.active,u.last_login_at as lastLoginAt,u.created_at as createdAt, coalesce(string_agg(r.code, ','), '') as roles from users u left join user_roles ur on ur.user_id=u.id left join roles r on r.id=ur.role_id group by u.id order by u.created_at desc");
  }

  @PostMapping("/users")
  public Map<String,Object> create(@AuthenticationPrincipal CurrentUser current, @Valid @RequestBody UserRequest request, jakarta.servlet.http.HttpServletRequest http) {
    if (jdbc.queryForObject("select count(*) from users where lower(email)=lower(?)", Integer.class, request.email()) > 0) throw new IllegalArgumentException("A user with this email already exists.");
    UUID id = UUID.randomUUID(); jdbc.update("insert into users(id,full_name,email,password_hash,mobile,active) values (?,?,?,?,?,true)", id, request.fullName(), request.email().toLowerCase(Locale.ROOT), encoder.encode(request.password()), request.mobile());
    assignRole(id, request.role()); audit.record(current.user().getId(), "USER", id, "CREATE", null, request.email(), http.getRemoteAddr());
    return jdbc.queryForMap("select id,full_name as fullName,email,mobile,active,created_at as createdAt from users where id=?", id);
  }

  @PutMapping("/users/{id}")
  public Map<String,Object> update(@AuthenticationPrincipal CurrentUser current, @PathVariable UUID id, @Valid @RequestBody UserUpdate request, jakarta.servlet.http.HttpServletRequest http) {
    Map<String,Object> before = jdbc.queryForMap("select id,full_name as fullName,email,mobile,active from users where id=?", id);
    int changed = jdbc.update("update users set full_name=?,mobile=?,active=?,updated_at=current_timestamp where id=?", request.fullName(), request.mobile(), request.active(), id);
    if (changed == 0) throw new NoSuchElementException("User not found.");
    if (request.role() != null && !request.role().isBlank()) { jdbc.update("delete from user_roles where user_id=?", id); assignRole(id, request.role()); }
    audit.record(current.user().getId(), "USER", id, "UPDATE", before.toString(), request.toString(), http.getRemoteAddr());
    return jdbc.queryForMap("select id,full_name as fullName,email,mobile,active,updated_at as updatedAt from users where id=?", id);
  }

  @PostMapping("/users/{id}/reset-password")
  public void resetPassword(@AuthenticationPrincipal CurrentUser current, @PathVariable UUID id, @RequestBody PasswordRequest request, jakarta.servlet.http.HttpServletRequest http) {
    if (jdbc.update("update users set password_hash=?,updated_at=current_timestamp where id=?", encoder.encode(request.password()), id) == 0) throw new NoSuchElementException("User not found.");
    jdbc.update("update refresh_tokens set revoked_at=current_timestamp where user_id=? and revoked_at is null", id);
    jdbc.update("update user_sessions set revoked_at=current_timestamp where user_id=? and revoked_at is null", id);
    audit.record(current.user().getId(), "USER", id, "RESET_PASSWORD", null, "credentials rotated", http.getRemoteAddr());
  }

  @PostMapping("/users/{id}/restore")
  public void restore(@AuthenticationPrincipal CurrentUser current, @PathVariable UUID id, jakarta.servlet.http.HttpServletRequest http) {
    if (jdbc.update("update users set active=true,updated_at=current_timestamp where id=?", id) == 0) throw new NoSuchElementException("User not found.");
    audit.record(current.user().getId(), "USER", id, "RESTORE", "inactive", "active", http.getRemoteAddr());
  }

  @GetMapping("/roles") public List<Map<String,Object>> roles() { return jdbc.queryForList("select id,code,name,description from roles order by name"); }
  @GetMapping("/permissions") public List<Map<String,Object>> permissions() { return jdbc.queryForList("select id,code,name,module from permissions order by module,code"); }
  @GetMapping("/audit-logs") public List<Map<String,Object>> auditLogs(@RequestParam(defaultValue="100") int limit) { return jdbc.queryForList("select a.id,a.entity_type as entityType,a.entity_id as entityId,a.action,a.before_data as beforeData,a.after_data as afterData,a.ip_address as ipAddress,a.created_at as createdAt,u.email as actor from audit_logs a left join users u on u.id=a.actor_id order by a.created_at desc limit ?", Math.min(Math.max(limit, 1), 500)); }

  private void assignRole(UUID userId, String role) { UUID roleId = jdbc.queryForObject("select id from roles where code=?", UUID.class, role.toUpperCase(Locale.ROOT)); if (roleId == null) throw new IllegalArgumentException("Unknown role."); jdbc.update("insert into user_roles(user_id,role_id) values (?,?)", userId, roleId); }
  public record UserRequest(@NotBlank String fullName, @Email @NotBlank String email, String mobile, @NotBlank String password, @NotBlank String role) {}
  public record UserUpdate(@NotBlank String fullName, String mobile, boolean active, String role) {}
  public record PasswordRequest(@NotBlank String password) {}
}
