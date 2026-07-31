package com.saivandan.crm.lead;

import com.saivandan.crm.user.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name = "leads")
public class Lead {
  @Id @GeneratedValue private UUID id;
  @Column(name = "lead_number", nullable = false, unique = true) private String leadNumber;
  @Column(name = "customer_name", nullable = false) private String customerName;
  @Column(nullable = false) private String mobile;
  private String email; private String city;
  @Column(name = "budget_min") private BigDecimal budgetMin;
  @Column(name = "budget_max") private BigDecimal budgetMax;
  @Column(name = "preferred_configuration") private String preferredConfiguration;
  @Column(nullable = false) private String source;
  @Enumerated(EnumType.STRING) @Column(nullable = false) private LeadStatus status = LeadStatus.NEW;
  private String temperature = "WARM";
  @ManyToOne @JoinColumn(name = "assigned_to") private AppUser assignedTo;
  @Column(name = "enquiry_date", nullable = false) private LocalDate enquiryDate = LocalDate.now();
  @Column(name = "loan_required") private Boolean loanRequired;
  @Column(name = "preferred_location") private String preferredLocation;
  @Column(name = "purchase_timeline") private String purchaseTimeline;
  @Column(name = "purchase_purpose") private String purchasePurpose;
  private String notes;
  @ManyToOne @JoinColumn(name = "created_by") private AppUser createdBy;
  @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
  @Column(name = "updated_at", nullable = false) private Instant updatedAt = Instant.now();
  @Column(name = "deleted_at") private Instant deletedAt;
  protected Lead() {}
  public Lead(String number, String customerName, String mobile, String source, AppUser creator) { this.leadNumber=number; this.customerName=customerName; this.mobile=mobile; this.source=source; this.createdBy=creator; this.assignedTo=creator; }
  @PreUpdate void touch() { updatedAt=Instant.now(); }
  public UUID getId(){return id;} public String getLeadNumber(){return leadNumber;} public String getCustomerName(){return customerName;} public String getMobile(){return mobile;} public String getEmail(){return email;} public String getCity(){return city;} public String getSource(){return source;} public LeadStatus getStatus(){return status;} public String getTemperature(){return temperature;} public AppUser getAssignedTo(){return assignedTo;} public LocalDate getEnquiryDate(){return enquiryDate;} public Boolean getLoanRequired(){return loanRequired;} public String getPreferredLocation(){return preferredLocation;} public String getPurchaseTimeline(){return purchaseTimeline;} public String getPurchasePurpose(){return purchasePurpose;} public String getNotes(){return notes;} public BigDecimal getBudgetMin(){return budgetMin;} public BigDecimal getBudgetMax(){return budgetMax;} public String getPreferredConfiguration(){return preferredConfiguration;} public Instant getCreatedAt(){return createdAt;}
  public void update(String customerName,String mobile,String email,String city,BigDecimal budgetMin,BigDecimal budgetMax,String config,String source,String temperature,Boolean loanRequired,String location,String timeline,String purpose,String notes) { this.customerName=customerName;this.mobile=mobile;this.email=email;this.city=city;this.budgetMin=budgetMin;this.budgetMax=budgetMax;this.preferredConfiguration=config;this.source=source;this.temperature=temperature;this.loanRequired=loanRequired;this.preferredLocation=location;this.purchaseTimeline=timeline;this.purchasePurpose=purpose;this.notes=notes; }
  public void assignTo(AppUser user){this.assignedTo=user;} public void setStatus(LeadStatus status){this.status=status;}
}

