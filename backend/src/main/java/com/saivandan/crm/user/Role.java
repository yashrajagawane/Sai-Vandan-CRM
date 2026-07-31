package com.saivandan.crm.user;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name = "roles")
public class Role {
  @Id @GeneratedValue private UUID id;
  @Enumerated(EnumType.STRING) @Column(nullable = false, unique = true) private RoleCode code;
  @Column(nullable = false) private String name;
  private String description;
  public UUID getId() { return id; }
  public RoleCode getCode() { return code; }
  public String getName() { return name; }
}

