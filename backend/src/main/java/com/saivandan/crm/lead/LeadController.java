package com.saivandan.crm.lead;

import com.saivandan.crm.security.CurrentUser;
import com.saivandan.crm.user.AppUser;
import com.saivandan.crm.user.AppUserRepository;
import com.saivandan.crm.user.RoleCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/leads")
public class LeadController {
  private final LeadRepository leads; private final LeadActivityRepository activities; private final AppUserRepository users;
  public LeadController(LeadRepository leads, LeadActivityRepository activities, AppUserRepository users){this.leads=leads;this.activities=activities;this.users=users;}
  @GetMapping public Page<LeadSummary> list(@AuthenticationPrincipal CurrentUser user, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
    PageRequest paging=PageRequest.of(Math.max(0,page), Math.min(Math.max(1,size),100), Sort.by(Sort.Direction.DESC,"createdAt"));
    Page<Lead> result=canSeeAll(user) ? leads.findByDeletedAtIsNull(paging) : leads.findByAssignedToIdAndDeletedAtIsNull(user.user().getId(), paging);
    return result.map(this::summary);
  }
  @PostMapping @ResponseStatus(HttpStatus.CREATED) public LeadSummary create(@AuthenticationPrincipal CurrentUser user,@Valid @RequestBody LeadRequest request) {
    if (leads.existsByMobileAndDeletedAtIsNull(request.mobile())) throw new IllegalArgumentException("A current lead already exists for this mobile number.");
    Lead lead=new Lead("LD-"+Year.now().getValue()+"-"+UUID.randomUUID().toString().substring(0,8).toUpperCase(),request.customerName(),request.mobile(),request.source(),user.user());
    apply(lead,request); return summary(leads.save(lead));
  }
  @GetMapping("/{id}") public LeadDetail get(@AuthenticationPrincipal CurrentUser user,@PathVariable UUID id){ Lead lead=visible(user,id); return detail(lead); }
  @PutMapping("/{id}") public LeadSummary update(@AuthenticationPrincipal CurrentUser user,@PathVariable UUID id,@Valid @RequestBody LeadRequest request){Lead lead=visible(user,id);apply(lead,request);return summary(leads.save(lead));}
  @PatchMapping("/{id}/assign/{assigneeId}") public LeadSummary assign(@AuthenticationPrincipal CurrentUser user,@PathVariable UUID id,@PathVariable UUID assigneeId){ if(!canAssign(user)) throw new org.springframework.security.access.AccessDeniedException("Lead assignment requires manager access"); Lead lead=visible(user,id); AppUser assignee=users.findById(assigneeId).orElseThrow(()->new IllegalArgumentException("Assignee not found"));lead.assignTo(assignee);return summary(leads.save(lead));}
  @PatchMapping("/{id}/status") public LeadSummary status(@AuthenticationPrincipal CurrentUser user,@PathVariable UUID id,@RequestParam LeadStatus status){Lead lead=visible(user,id);lead.setStatus(status);return summary(leads.save(lead));}
  @PostMapping("/{id}/activities") @ResponseStatus(HttpStatus.CREATED) public ActivityResponse addActivity(@AuthenticationPrincipal CurrentUser user,@PathVariable UUID id,@Valid @RequestBody ActivityRequest request){Lead lead=visible(user,id);LeadActivity activity=activities.save(new LeadActivity(lead,request.type(),request.outcome(),request.remarks(),request.scheduledAt(),request.nextFollowUpAt(),user.user()));return activity(activity);}
  private Lead visible(CurrentUser user,UUID id){Lead lead=leads.findById(id).orElseThrow(()->new IllegalArgumentException("Lead not found"));if(!canSeeAll(user)&&!lead.getAssignedTo().getId().equals(user.user().getId()))throw new org.springframework.security.access.AccessDeniedException("You cannot access this lead");return lead;}
  private boolean canSeeAll(CurrentUser user){return user.user().getRoles().stream().map(r->r.getCode()).anyMatch(r->r==RoleCode.SUPER_ADMIN||r==RoleCode.ADMIN||r==RoleCode.MANAGEMENT||r==RoleCode.SALES_MANAGER||r==RoleCode.AUDITOR);}
  private boolean canAssign(CurrentUser user){return user.user().getRoles().stream().map(r->r.getCode()).anyMatch(r->r==RoleCode.SUPER_ADMIN||r==RoleCode.ADMIN||r==RoleCode.SALES_MANAGER);}
  private void apply(Lead lead,LeadRequest r){lead.update(r.customerName(),r.mobile(),r.email(),r.city(),r.budgetMin(),r.budgetMax(),r.preferredConfiguration(),r.source(),r.temperature(),r.loanRequired(),r.preferredLocation(),r.purchaseTimeline(),r.purchasePurpose(),r.notes());}
  private LeadSummary summary(Lead l){return new LeadSummary(l.getId(),l.getLeadNumber(),l.getCustomerName(),l.getMobile(),l.getSource(),l.getStatus().name(),l.getTemperature(),l.getAssignedTo()==null?null:l.getAssignedTo().getFullName(),l.getCreatedAt());}
  private LeadDetail detail(Lead l){return new LeadDetail(summary(l),l.getEmail(),l.getCity(),l.getBudgetMin(),l.getBudgetMax(),l.getPreferredConfiguration(),l.getLoanRequired(),l.getPreferredLocation(),l.getPurchaseTimeline(),l.getPurchasePurpose(),l.getNotes(),activities.findByLeadIdOrderByCreatedAtDesc(l.getId()).stream().map(this::activity).toList());}
  private ActivityResponse activity(LeadActivity a){return new ActivityResponse(a.getId(),a.getType(),a.getOutcome(),a.getRemarks(),a.getScheduledAt(),a.getCompletedAt(),a.getNextFollowUpAt(),a.getCreatedBy().getFullName());}
  public record LeadRequest(@NotBlank String customerName,@NotBlank String mobile,String email,String city,BigDecimal budgetMin,BigDecimal budgetMax,String preferredConfiguration,@NotBlank String source,String temperature,Boolean loanRequired,String preferredLocation,String purchaseTimeline,String purchasePurpose,String notes){}
  public record LeadSummary(UUID id,String leadNumber,String customerName,String mobile,String source,String status,String temperature,String assignedTo,Instant createdAt){}
  public record LeadDetail(LeadSummary lead,String email,String city,BigDecimal budgetMin,BigDecimal budgetMax,String configuration,Boolean loanRequired,String preferredLocation,String purchaseTimeline,String purchasePurpose,String notes,List<ActivityResponse> activities){}
  public record ActivityRequest(@NotBlank String type,String outcome,String remarks,Instant scheduledAt,Instant nextFollowUpAt){}
  public record ActivityResponse(UUID id,String type,String outcome,String remarks,Instant scheduledAt,Instant completedAt,Instant nextFollowUpAt,String createdBy){}
}
