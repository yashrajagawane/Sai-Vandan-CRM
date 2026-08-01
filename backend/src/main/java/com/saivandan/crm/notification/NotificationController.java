package com.saivandan.crm.notification;

import com.saivandan.crm.security.AuditService;
import com.saivandan.crm.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
  private final JdbcTemplate jdbc; private final AuditService audit;
  public NotificationController(JdbcTemplate jdbc, AuditService audit){this.jdbc=jdbc;this.audit=audit;}

  @GetMapping @PreAuthorize("isAuthenticated()")
  public List<Map<String,Object>> list(@RequestParam(defaultValue="false") boolean unreadOnly,@RequestParam(defaultValue="40") int limit,@AuthenticationPrincipal CurrentUser current){
    escalateDue();
    int safeLimit=Math.min(Math.max(limit,1),100); List<String> roles=current.user().getRoles().stream().map(r->r.getCode().name()).toList();
    String roleSql=roles.stream().map(r->"?").collect(Collectors.joining(","));
    String sql="select id,event_type as eventType,title,message,severity,deep_link as deepLink,due_at as dueAt,escalated_at as escalatedAt,read_at as readAt,created_at as createdAt from notifications where (recipient_user_id=? or role_code in ("+roleSql+"))"+(unreadOnly?" and read_at is null":"")+" order by created_at desc limit "+safeLimit;
    List<Object> args=new ArrayList<>(); args.add(current.user().getId()); args.addAll(roles); return jdbc.queryForList(sql,args.toArray());
  }

  @GetMapping("/unread-count") @PreAuthorize("isAuthenticated()")
  public Map<String,Object> unreadCount(@AuthenticationPrincipal CurrentUser current){escalateDue();List<String> roles=current.user().getRoles().stream().map(r->r.getCode().name()).toList();String roleSql=roles.stream().map(r->"?").collect(Collectors.joining(","));List<Object> args=new ArrayList<>();args.add(current.user().getId());args.addAll(roles);Long count=jdbc.queryForObject("select count(*) from notifications where (recipient_user_id=? or role_code in ("+roleSql+")) and read_at is null",Long.class,args.toArray());return Map.of("unread",count==null?0:count);}

  @GetMapping("/preferences") @PreAuthorize("isAuthenticated()")
  public Map<String,Object> preferences(@AuthenticationPrincipal CurrentUser current){Long exists=jdbc.queryForObject("select count(*) from notification_preferences where user_id=?",Long.class,current.user().getId());if(exists==null||exists==0)jdbc.update("insert into notification_preferences(user_id) values (?)",current.user().getId());return jdbc.queryForMap("select user_id as userId,in_app_enabled as inAppEnabled,email_enabled as emailEnabled,reminder_enabled as reminderEnabled,updated_at as updatedAt from notification_preferences where user_id=?",current.user().getId());}

  @PutMapping("/preferences") @PreAuthorize("isAuthenticated()")
  public Map<String,Object> updatePreferences(@RequestBody PreferenceRequest req,@AuthenticationPrincipal CurrentUser current){int updated=jdbc.update("update notification_preferences set in_app_enabled=?,email_enabled=?,reminder_enabled=?,updated_at=current_timestamp where user_id=?",req.inAppEnabled()==null||req.inAppEnabled(),req.emailEnabled()!=null&&req.emailEnabled(),req.reminderEnabled()==null||req.reminderEnabled(),current.user().getId());if(updated==0)jdbc.update("insert into notification_preferences(user_id,in_app_enabled,email_enabled,reminder_enabled) values (?,?,?,?)",current.user().getId(),req.inAppEnabled()==null||req.inAppEnabled(),req.emailEnabled()!=null&&req.emailEnabled(),req.reminderEnabled()==null||req.reminderEnabled());return preferences(current);}

  @PatchMapping("/{id}/read") @PreAuthorize("isAuthenticated()")
  public Map<String,Object> markRead(@PathVariable UUID id,@AuthenticationPrincipal CurrentUser current){jdbc.update("update notifications set read_at=current_timestamp where id=? and (recipient_user_id=? or role_code in (select code from roles r join user_roles ur on ur.role_id=r.id where ur.user_id=?))",id,current.user().getId(),current.user().getId());return jdbc.queryForMap("select id,read_at as readAt from notifications where id=?",id);}

  @PostMapping("/read-all") @PreAuthorize("isAuthenticated()")
  public Map<String,Object> markAllRead(@AuthenticationPrincipal CurrentUser current){List<String> roles=current.user().getRoles().stream().map(r->r.getCode().name()).toList();String roleSql=roles.stream().map(r->"?").collect(Collectors.joining(","));List<Object> args=new ArrayList<>();args.add(current.user().getId());args.addAll(roles);int updated=jdbc.update("update notifications set read_at=current_timestamp where read_at is null and (recipient_user_id=? or role_code in ("+roleSql+"))",args.toArray());return Map.of("updated",updated);}

  @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('SUPER_ADMIN')")
  public Map<String,Object> create(@Valid @RequestBody NotificationRequest req,@AuthenticationPrincipal CurrentUser current){UUID id=UUID.randomUUID();jdbc.update("insert into notifications(id,recipient_user_id,role_code,event_type,title,message,severity,deep_link,due_at) values (?,?,?,?,?,?,?,?,?)",id,req.recipientUserId(),req.roleCode(),req.eventType(),req.title(),req.message(),req.severity()==null?"INFO":req.severity(),req.deepLink(),req.dueAt());audit.record(current.user().getId(),"NOTIFICATION",id,"CREATE",null,req.title(),null);return jdbc.queryForMap("select id,event_type as eventType,title,message,severity,deep_link as deepLink,due_at as dueAt,read_at as readAt,created_at as createdAt from notifications where id=?",id);}

  public record NotificationRequest(UUID recipientUserId,String roleCode,@NotBlank String eventType,@NotBlank String title,@NotBlank String message,String severity,String deepLink,Instant dueAt){}
  public record PreferenceRequest(Boolean inAppEnabled,Boolean emailEnabled,Boolean reminderEnabled){}
  private void escalateDue(){jdbc.update("update notifications set escalated_at=current_timestamp,severity='ESCALATED' where due_at<current_timestamp and read_at is null and escalated_at is null");}
}
