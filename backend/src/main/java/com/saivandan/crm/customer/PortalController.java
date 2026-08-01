package com.saivandan.crm.customer;

import com.saivandan.crm.security.AuditService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/portal")
public class PortalController {
  private final JdbcTemplate jdbc; private final AuditService audit;
  public PortalController(JdbcTemplate jdbc, AuditService audit){this.jdbc=jdbc;this.audit=audit;}

  @PostMapping("/access")
  public Map<String,Object> access(@Valid @RequestBody PortalAccessRequest req){Map<String,Object> customer=jdbc.queryForMap("select c.id,c.full_name as fullName,c.mobile,c.email,c.booking_id as bookingId from customers c join bookings b on b.id=c.booking_id where lower(c.email)=lower(?) and (? is null or b.booking_number=?)",req.email(),req.bookingNumber(),req.bookingNumber());String token=UUID.randomUUID().toString()+UUID.randomUUID();jdbc.update("insert into portal_access_tokens(customer_id,token_hash,expires_at) values (?,?,?)",customer.get("id"),hash(token),Instant.now().plusSeconds(86400));return Map.of("portalToken",token,"expiresAt",Instant.now().plusSeconds(86400),"customer",customer.get("fullName"));}

  @GetMapping("/me")
  public Map<String,Object> me(@RequestHeader("X-Portal-Token") String token){UUID customerId=customerId(token);Map<String,Object> customer=jdbc.queryForMap("select id,customer_number as customerNumber,full_name as fullName,mobile,email,status from customers where id=?",customerId);UUID bookingId=jdbc.queryForObject("select booking_id from customers where id=?",UUID.class,customerId);Map<String,Object> booking=jdbc.queryForMap("select b.booking_number as bookingNumber,b.booking_date as bookingDate,b.status,b.booking_amount as bookingAmount,u.unit_number as unit,u.configuration,u.carpet_area as carpetArea,u.base_price as unitPrice,p.name as project from bookings b join units u on u.id=b.unit_id join projects p on p.id=u.project_id where b.id=?",bookingId);Map<String,Object> result=new LinkedHashMap<>();result.put("customer",customer);result.put("booking",booking);result.put("payments",jdbc.queryForList("select receipt_number as receiptNumber,payment_type as paymentType,amount,payment_date as paymentDate,status from customer_payments where booking_id=? and status<>'REVERSED' order by payment_date desc",bookingId));result.put("installments",jdbc.queryForList("select installment_type as installmentType,due_date as dueDate,amount,paid_amount as paidAmount,status from payment_installments where booking_id=? order by sequence_no",bookingId));result.put("documents",jdbc.queryForList("select document_type as documentType,file_name as fileName,verification_status as verificationStatus,masked,expiry_date as expiryDate from customer_documents where booking_id=? order by document_type",bookingId));result.put("loan",single("select status,bank_name as bankName,loan_amount as loanAmount,emi,sanction_date as sanctionDate from loan_applications where booking_id=?",bookingId));result.put("agreement",single("select agreement_date as agreementDate,agreement_value as agreementValue,stamp_duty as stampDuty,registration_date as registrationDate,registration_number as registrationNumber,status from agreements where booking_id=?",bookingId));result.put("possession",single("select status,scheduled_date as scheduledDate,signoff_name as signoffName from possession_cases where booking_id=?",bookingId));result.put("tickets",jdbc.queryForList("select ticket_number as ticketNumber,category,priority,status,subject,description,due_at as dueAt from support_tickets where booking_id=? order by due_at desc",bookingId));result.put("referrals",jdbc.queryForList("select referred_name as referredName,referred_mobile as referredMobile,status,reward_amount as rewardAmount from referrals where source_booking_id=? order by created_at desc",bookingId));return result;}

  @PostMapping("/tickets") @ResponseStatus(HttpStatus.CREATED)
  public Map<String,Object> ticket(@RequestHeader("X-Portal-Token") String token,@Valid @RequestBody PortalTicketRequest req){UUID customerId=customerId(token);UUID bookingId=jdbc.queryForObject("select booking_id from customers where id=?",UUID.class,customerId);UUID id=UUID.randomUUID();String number="SUP-PORTAL-"+UUID.randomUUID().toString().substring(0,8).toUpperCase();jdbc.update("insert into support_tickets(id,ticket_number,booking_id,category,priority,status,subject,description,due_at) values (?,?,?,?,?,'OPEN',?,?,?)",id,number,bookingId,req.category(),req.priority(),req.subject(),req.description(),Instant.now().plusSeconds(259200));return jdbc.queryForMap("select id,ticket_number as ticketNumber,category,priority,status,subject,description,due_at as dueAt from support_tickets where id=?",id);}

  @PostMapping("/revoke") @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revoke(@RequestHeader("X-Portal-Token") String token){jdbc.update("update portal_access_tokens set revoked_at=current_timestamp where token_hash=?",hash(token));}

  private UUID customerId(String token){if(token==null||token.isBlank())throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Portal token is required.");List<UUID> ids=jdbc.query("select customer_id from portal_access_tokens where token_hash=? and revoked_at is null and expires_at>current_timestamp",(rs,n)->(UUID)rs.getObject(1),hash(token));if(ids.isEmpty())throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Portal token is invalid or expired.");return ids.get(0);}
  private Map<String,Object> single(String sql,Object arg){List<Map<String,Object>> rows=jdbc.queryForList(sql,arg);return rows.isEmpty()?Map.of("status","NOT_AVAILABLE"):rows.get(0);}
  private String hash(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception ex){throw new IllegalStateException(ex);}}
  public record PortalAccessRequest(@NotBlank @Email String email,String bookingNumber){}
  public record PortalTicketRequest(@NotBlank String category,@NotBlank String priority,@NotBlank String subject,@NotBlank String description){}
}
