package com.saivandan.crm.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.UUID;

@Service
public class AuditService {
  private final JdbcTemplate jdbc;
  private final ObjectMapper objectMapper;
  private final DataSource dataSource;

  public AuditService(JdbcTemplate jdbc, ObjectMapper objectMapper) {
    this.jdbc = jdbc;
    this.objectMapper = objectMapper;
    this.dataSource = jdbc.getDataSource();
  }

  public void record(UUID actorId, String entityType, UUID entityId, String action, String before, String after, String ip) {
    String sql = "insert into audit_logs(actor_id,entity_type,entity_id,action,before_data,after_data,ip_address) values (?,?,?,?,?,?,?)";
    Object beforeValue = before;
    Object afterValue = after;
    if (isPostgres()) {
      // Production stores audit payloads as JSONB. Normalize legacy text payloads
      // into valid JSON strings before casting, while preserving JSON objects.
      sql = "insert into audit_logs(actor_id,entity_type,entity_id,action,before_data,after_data,ip_address) values (?,?,?,?,?::jsonb,?::jsonb,?)";
      beforeValue = jsonValue(before);
      afterValue = jsonValue(after);
    }
    jdbc.update(sql, actorId, entityType, entityId, action, beforeValue, afterValue, ip);
  }

  private boolean isPostgres() {
    if (dataSource == null) return false;
    Connection connection = DataSourceUtils.getConnection(dataSource);
    try {
      return connection.getMetaData().getDatabaseProductName().toLowerCase().contains("postgres");
    } catch (Exception ex) {
      return false;
    } finally {
      DataSourceUtils.releaseConnection(connection, dataSource);
    }
  }

  private String jsonValue(String value) {
    String text = truncate(value);
    if (text == null) return null;
    try {
      objectMapper.readTree(text);
      return text;
    } catch (Exception ignored) {
      try {
        return objectMapper.writeValueAsString(text);
      } catch (Exception ex) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
      }
    }
  }

  private String truncate(String value) { return value == null ? null : value.length() > 10000 ? value.substring(0, 10000) : value; }
}
