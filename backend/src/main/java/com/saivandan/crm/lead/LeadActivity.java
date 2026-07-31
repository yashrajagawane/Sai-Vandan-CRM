package com.saivandan.crm.lead;

import com.saivandan.crm.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "lead_activities")
public class LeadActivity {
  @Id @GeneratedValue private UUID id;
  @ManyToOne @JoinColumn(name = "lead_id", nullable = false) private Lead lead;
  @Column(nullable = false) private String type; private String outcome; private String remarks;
  @Column(name = "scheduled_at") private Instant scheduledAt; @Column(name = "completed_at") private Instant completedAt; @Column(name = "next_follow_up_at") private Instant nextFollowUpAt;
  @ManyToOne @JoinColumn(name = "created_by") private AppUser createdBy; @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
  protected LeadActivity(){} public LeadActivity(Lead lead,String type,String outcome,String remarks,Instant scheduledAt,Instant nextFollowUpAt,AppUser createdBy){this.lead=lead;this.type=type;this.outcome=outcome;this.remarks=remarks;this.scheduledAt=scheduledAt;this.nextFollowUpAt=nextFollowUpAt;this.createdBy=createdBy;this.completedAt=Instant.now();}
  public UUID getId(){return id;} public String getType(){return type;} public String getOutcome(){return outcome;} public String getRemarks(){return remarks;} public Instant getScheduledAt(){return scheduledAt;} public Instant getCompletedAt(){return completedAt;} public Instant getNextFollowUpAt(){return nextFollowUpAt;} public AppUser getCreatedBy(){return createdBy;}
}

