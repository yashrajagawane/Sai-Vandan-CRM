package com.saivandan.crm.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class AuditService {
  private final JdbcTemplate jdbc;
  public AuditService(JdbcTemplate jdbc) { this.jdbc = jdbc; }
  public void record(UUID actorId, String entityType, UUID entityId, String action, String before, String after, String ip) {
    jdbc.update("insert into audit_logs(actor_id,entity_type,entity_id,action,before_data,after_data,ip_address) values (?,?,?,?,?,?,?)",
      actorId, entityType, entityId, action, truncate(before), truncate(after), ip);
  }
  private String truncate(String value) { return value == null ? null : value.length() > 10000 ? value.substring(0, 10000) : value; }
}
