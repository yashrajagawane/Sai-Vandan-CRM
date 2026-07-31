package com.saivandan.crm.workspace;

import com.saivandan.crm.security.CurrentUser;
import com.saivandan.crm.user.RoleCode;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;

@RestController
public class WorkspaceController {
  private final JdbcTemplate jdbc;
  public WorkspaceController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @GetMapping("/dashboard")
  public DashboardResponse dashboard(@AuthenticationPrincipal CurrentUser user) {
    RoleCode role = roleOf(user); UUID userId = user.user().getId(); boolean salesScope = role == RoleCode.SALES_EXECUTIVE || role == RoleCode.TELECALLER;
    long leads = count(salesScope ? "select count(*) from leads where deleted_at is null and assigned_to=?" : "select count(*) from leads where deleted_at is null", salesScope ? userId : null);
    long visits = count(salesScope ? "select count(*) from site_visits where executive_id=? and status='SCHEDULED'" : "select count(*) from site_visits where status='SCHEDULED'", salesScope ? userId : null);
    long pendingPayments = count("select count(*) from customer_payments where status='PENDING'", null);
    long supportOpen = count("select count(*) from support_tickets where status in ('OPEN','IN_PROGRESS')", null);
    List<Metric> metrics = switch (role) {
      case SUPER_ADMIN, ADMIN -> List.of(metric("Active leads", leads, "CRM pipeline"), metric("Available units", count("select count(*) from units where status='AVAILABLE'", null), "Live inventory"), metric("Pending collections", pendingPayments, "Finance action"), metric("Open support tickets", supportOpen, "Customer care"));
      case MANAGEMENT -> List.of(metric("Qualified pipeline", count("select count(*) from leads where status in ('QUALIFIED','NEGOTIATION','VISITED')", null), "Review sales health"), metric("Confirmed bookings", count("select count(*) from bookings where status='CONFIRMED'", null), "Current period"), moneyMetric("Collections received", money("select coalesce(sum(amount),0) from customer_payments where status='PAID'"), "Realised amount"), moneyMetric("Vendor outstanding", money("select coalesce(sum(amount),0) from vendor_bills where status in ('PENDING','OVERDUE')"), "Approval focus"));
      case SALES_MANAGER -> List.of(metric("Team leads", leads, "Across the sales team"), metric("Site visits", visits, "Scheduled today"), metric("Negotiations", count("select count(*) from leads where status='NEGOTIATION'", null), "Need approval"), metric("Confirmed bookings", count("select count(*) from bookings where status='CONFIRMED'", null), "Team conversion"));
      case SALES_EXECUTIVE -> List.of(metric("My active leads", leads, "Assigned to you"), metric("My visits", visits, "Upcoming"), metric("Hot prospects", count("select count(*) from leads where assigned_to=? and temperature='HOT'", userId), "Follow up today"), metric("My negotiations", count("select count(*) from leads where assigned_to=? and status='NEGOTIATION'", userId), "Move to booking"));
      case TELECALLER -> List.of(metric("My call leads", leads, "Assigned to you"), metric("New enquiries", count("select count(*) from leads where assigned_to=? and status='NEW'", userId), "Call first"), metric("Qualified", count("select count(*) from leads where assigned_to=? and status='QUALIFIED'", userId), "Ready to handover"), metric("Future prospects", count("select count(*) from leads where assigned_to=? and status='FUTURE_PROSPECT'", userId), "Nurture queue"));
      case RECEPTION -> List.of(metric("Walk-in leads", count("select count(*) from leads where source='Walk-in'", null), "Visitor desk"), metric("Today's visits", visits, "Check-in queue"), metric("Available units", count("select count(*) from units where status='AVAILABLE'", null), "Share availability"), metric("New enquiries", count("select count(*) from leads where status='NEW'", null), "Registration desk"));
      case INVENTORY_MANAGER -> List.of(metric("Available units", count("select count(*) from units where status='AVAILABLE'", null), "Ready to sell"), metric("Reserved units", count("select count(*) from units where status='RESERVED'", null), "Reservation review"), metric("Booked units", count("select count(*) from units where status='BOOKED'", null), "Sales handover"), metric("Sold units", count("select count(*) from units where status='SOLD'", null), "Inventory status"));
      case ACCOUNTS_MANAGER, ACCOUNTS_EXECUTIVE -> List.of(moneyMetric("Collections received", money("select coalesce(sum(amount),0) from customer_payments where status='PAID'"), "Today and historical"), metric("Pending receipts", pendingPayments, "Due collection"), moneyMetric("Vendor payable", money("select coalesce(sum(amount),0) from vendor_bills where status in ('PENDING','OVERDUE')"), "Payment queue"), moneyMetric("Petty cash", money("select coalesce(sum(amount),0) from petty_cash_entries where status='APPROVED'"), "Approved expenses"));
      case HR_MANAGER, HR_EXECUTIVE -> List.of(metric("Active employees", count("select count(*) from employees where active=true", null), "Employee master"), moneyMetric("Monthly basic", money("select coalesce(sum(basic_salary),0) from employees where active=true"), "Payroll base"), metric("Sales incentives", count("select count(*) from bookings where status='CONFIRMED'", null), "Commission eligible"), metric("Leave approvals", 3, "Pending review"));
      case PROCUREMENT_MANAGER -> List.of(metric("Active vendors", count("select count(*) from vendors where active=true", null), "Vendor master"), moneyMetric("Bills pending", money("select coalesce(sum(amount),0) from vendor_bills where status='PENDING'"), "Payment planning"), metric("Overdue bills", count("select count(*) from vendor_bills where status='OVERDUE'", null), "Escalate today"), metric("Purchase items", 8, "Project supplies"));
      case LEGAL_OFFICER -> List.of(metric("Confirmed bookings", count("select count(*) from bookings where status='CONFIRMED'", null), "Agreement queue"), metric("Documents pending", 4, "Verify documents"), metric("Loan cases", 2, "Bank follow-up"), metric("Registration due", 1, "Legal calendar"));
      case SUPPORT_EXECUTIVE -> List.of(metric("Open tickets", supportOpen, "Assigned support"), metric("High priority", count("select count(*) from support_tickets where priority='HIGH' and status='OPEN'", null), "Act now"), metric("Possession cases", 2, "Handover support"), metric("Customer referrals", 3, "Follow-up list"));
      case AUDITOR -> List.of(metric("Audit records", count("select count(*) from leads", null) + count("select count(*) from customer_payments", null), "Read-only review"), moneyMetric("Receivables", money("select coalesce(sum(amount),0) from customer_payments where status='PENDING'"), "Financial review"), moneyMetric("Vendor outstanding", money("select coalesce(sum(amount),0) from vendor_bills where status in ('PENDING','OVERDUE')"), "Ledger review"), metric("Approval exceptions", 2, "Audit trail"));
      case CUSTOMER -> List.of(metric("My booked unit", 1, "B-102"), moneyMetric("Amount paid", money("select coalesce(sum(amount),0) from customer_payments where status='PAID'"), "Receipt available"), moneyMetric("Amount due", money("select coalesce(sum(amount),0) from customer_payments where status='PENDING'"), "Next payment due"), metric("Service requests", supportOpen, "Track support"));
    };
    return new DashboardResponse(role.name(), title(role), subtitle(role), metrics, queue(role, userId), allowedModules(role));
  }

  @GetMapping("/workspace/{module}")
  public WorkspaceResponse workspace(@AuthenticationPrincipal CurrentUser user, @PathVariable String module) {
    RoleCode role = roleOf(user); String key = module.toLowerCase(Locale.ROOT);
    if (!allowedModules(role).contains(key)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This module is not available for your role.");
    List<Map<String,Object>> rows = switch(key) {
      case "inventory" -> jdbc.queryForList("select unit_number as unit, wing, floor, configuration, status, base_price as price from units order by unit_number");
      case "site-visits" -> jdbc.queryForList("select l.customer_name as customer, s.visit_date as date, s.visit_time as time, s.status, s.feedback from site_visits s join leads l on l.id=s.lead_id order by s.visit_date desc");
      case "collections" -> jdbc.queryForList("select p.receipt_number as receipt, b.booking_number as booking, p.payment_type as type, p.amount, p.due_date as due, p.status from customer_payments p join bookings b on b.id=p.booking_id order by p.due_date");
      case "reports" -> jdbc.queryForList("select 'Lead pipeline' as report, cast(count(*) as varchar) as value, 'Live CRM leads' as note from leads union all select 'Vendor outstanding', cast(coalesce(sum(amount),0) as varchar), 'Bills awaiting payment' from vendor_bills where status in ('PENDING','OVERDUE') union all select 'Active employees', cast(count(*) as varchar), 'Payroll master' from employees where active=true");
      case "leads" -> jdbc.queryForList("select lead_number as lead, customer_name as customer, source, status, temperature from leads where deleted_at is null order by created_at desc");
      default -> List.of(Map.of("module", key, "status", "Ready for implementation"));
    };
    return new WorkspaceResponse(key, moduleTitle(key), rows, role.name());
  }

  private RoleCode roleOf(CurrentUser user) { return user.user().getRoles().stream().findFirst().orElseThrow().getCode(); }
  private long count(String sql, Object parameter) { Long value = parameter == null ? jdbc.queryForObject(sql, Long.class) : jdbc.queryForObject(sql, Long.class, parameter); return value == null ? 0 : value; }
  private BigDecimal money(String sql) { BigDecimal value = jdbc.queryForObject(sql, BigDecimal.class); return value == null ? BigDecimal.ZERO : value; }
  private Metric metric(String label, long value, String note) { return new Metric(label, String.valueOf(value), note, false); }
  private Metric moneyMetric(String label, BigDecimal value, String note) { return new Metric(label, "INR " + value.setScale(0, java.math.RoundingMode.HALF_UP), note, true); }
  private String title(RoleCode role) { return switch(role) { case CUSTOMER -> "Your home, at a glance"; case AUDITOR -> "Audit & assurance workspace"; case MANAGEMENT -> "Executive command centre"; case HR_MANAGER, HR_EXECUTIVE -> "People & payroll workspace"; case PROCUREMENT_MANAGER -> "Vendor & procurement workspace"; case LEGAL_OFFICER -> "Legal & documentation desk"; case SUPPORT_EXECUTIVE -> "Customer care workspace"; case INVENTORY_MANAGER -> "Inventory control centre"; case ACCOUNTS_MANAGER, ACCOUNTS_EXECUTIVE -> "Finance control centre"; default -> "Today at a glance"; }; }
  private String subtitle(RoleCode role) { return role.name().replace('_',' ') + " - role-specific operational view"; }
  private List<QueueItem> queue(RoleCode role, UUID userId) { return switch(role) { case SALES_EXECUTIVE, TELECALLER -> List.of(new QueueItem("Follow-ups due", count("select count(*) from leads where assigned_to=? and status in ('NEW','QUALIFIED','FUTURE_PROSPECT')",userId),"Open your lead queue"),new QueueItem("Scheduled visits",count("select count(*) from site_visits where executive_id=? and status='SCHEDULED'",userId),"Confirm customer visit")); case ACCOUNTS_MANAGER, ACCOUNTS_EXECUTIVE -> List.of(new QueueItem("Payment follow-up",count("select count(*) from customer_payments where status='PENDING'",null),"Collect due installments"),new QueueItem("Vendor bills",count("select count(*) from vendor_bills where status in ('PENDING','OVERDUE')",null),"Review payment dates")); case SUPPORT_EXECUTIVE -> List.of(new QueueItem("Open customer requests",count("select count(*) from support_tickets where status='OPEN'",null),"Resolve within SLA"),new QueueItem("High priority",count("select count(*) from support_tickets where priority='HIGH'",null),"Escalate immediately")); default -> List.of(new QueueItem("Action items",3,"Role-specific approvals and reviews"),new QueueItem("Notifications",2,"New updates since your last sign-in")); }; }
  private List<String> allowedModules(RoleCode role) { return switch(role) { case CUSTOMER -> List.of("collections"); case SUPPORT_EXECUTIVE -> List.of("leads"); case HR_MANAGER, HR_EXECUTIVE, PROCUREMENT_MANAGER, LEGAL_OFFICER, AUDITOR -> List.of("reports"); case ACCOUNTS_MANAGER, ACCOUNTS_EXECUTIVE -> List.of("collections","reports"); case INVENTORY_MANAGER -> List.of("inventory","reports"); case RECEPTION -> List.of("leads","site-visits"); case TELECALLER -> List.of("leads"); default -> List.of("leads","inventory","site-visits","collections","reports"); }; }
  private String moduleTitle(String module) { return switch(module) { case "site-visits" -> "Site visit schedule"; case "collections" -> "Collections & receivables"; case "inventory" -> "Project inventory"; case "reports" -> "Operational reports"; default -> "Lead pipeline"; }; }
  public record Metric(String label, String value, String note, boolean money) {}
  public record QueueItem(String label, long count, String note) {}
  public record DashboardResponse(String role, String title, String subtitle, List<Metric> metrics, List<QueueItem> queue, List<String> modules) {}
  public record WorkspaceResponse(String module, String title, List<Map<String,Object>> rows, String role) {}
}
