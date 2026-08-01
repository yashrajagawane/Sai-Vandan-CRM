package com.saivandan.crm.config;

import com.saivandan.crm.user.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.UUID;

@Configuration
class SeedDataConfiguration {
  @Bean CommandLineRunner seedAdmin(AppUserRepository users, RoleRepository roles, PasswordEncoder encoder, JdbcTemplate jdbc) {
    return args -> {
      List<DemoAccount> accounts = List.of(
        new DemoAccount("Sai Vandan Super Admin", "admin@saivandan.local", RoleCode.SUPER_ADMIN),
        new DemoAccount("Rahul Deshmukh", "sales.manager@saivandan.local", RoleCode.SALES_MANAGER),
        new DemoAccount("Priya Sharma", "sales.executive@saivandan.local", RoleCode.SALES_EXECUTIVE),
        new DemoAccount("Pooja Nair", "hr@saivandan.local", RoleCode.HR),
        new DemoAccount("Neha Shah", "finance@saivandan.local", RoleCode.FINANCE),
        new DemoAccount("Sanjay Patil", "vendor@saivandan.local", RoleCode.VENDOR),
        new DemoAccount("Sneha More", "support@saivandan.local", RoleCode.SUPPORT)
      );
      for (DemoAccount account : accounts) {
        if (users.findByEmailIgnoreCase(account.email()).isEmpty()) {
          Role role = roles.findByCode(account.role()).orElseThrow();
          AppUser user = new AppUser(account.name(), account.email(), encoder.encode("ChangeMe!2026"));
          user.addRole(role); users.save(user);
        }
      }
      seedOperationalData(jdbc, users);
    };
  }
  private void seedOperationalData(JdbcTemplate jdbc, AppUserRepository users) {
    Integer existing = jdbc.queryForObject("select count(*) from projects", Integer.class);
    if (existing != null && existing > 0) return;
    jdbc.update("insert into projects(code,name,city,status) values ('SVC-01','Sai Vandan Complex','Pune','ACTIVE')");
    UUID project = jdbc.queryForObject("select id from projects where code='SVC-01'", UUID.class);
    String[][] units = {{"A","1","A-101","1 BHK","AVAILABLE","4200000"},{"A","2","A-201","2 BHK","AVAILABLE","6800000"},{"A","3","A-301","2 BHK","RESERVED","7100000"},{"B","1","B-102","2 BHK","BOOKED","6950000"},{"B","4","B-401","3 BHK","AVAILABLE","9800000"},{"B","5","B-501","3 BHK","SOLD","10500000"},{"C","2","C-202","4 BHK","AVAILABLE","14500000"}};
    for (String[] u : units) jdbc.update("insert into units(project_id,wing,floor,unit_number,configuration,carpet_area,built_up_area,base_price,status) values (?,?,?,?,?,?,?,?,?)", project,u[0],u[1],u[2],u[3],850,1100,new java.math.BigDecimal(u[5]),u[4]);
    UUID salesExecutive = userId(users,"sales.executive@saivandan.local"); UUID salesManager = userId(users,"sales.manager@saivandan.local");
    Object[][] leads = {{"LD-2026-0001","Aarav Mehta","9876543210","aarav@example.com","Pune","Website","QUALIFIED","HOT",salesExecutive,"2 BHK"},{"LD-2026-0002","Riya Kapoor","9876543211","riya@example.com","Mumbai","Referral","SITE_VISIT_SCHEDULED","HOT",salesExecutive,"3 BHK"},{"LD-2026-0003","Dev Malhotra","9876543212","dev@example.com","Pune","Google Ads","NEW","WARM",salesExecutive,"2 BHK"},{"LD-2026-0004","Nisha Iyer","9876543213","nisha@example.com","Nashik","Walk-in","NEGOTIATION","HOT",salesManager,"3 BHK"},{"LD-2026-0005","Rohan Shah","9876543214","rohan@example.com","Pune","WhatsApp","FUTURE_PROSPECT","COLD",salesExecutive,"1 BHK"},{"LD-2026-0006","Isha Desai","9876543215","isha@example.com","Pune","Facebook / Instagram","VISITED","WARM",salesExecutive,"2 BHK"}};
    for (Object[] l : leads) jdbc.update("insert into leads(lead_number,customer_name,mobile,email,city,source,status,temperature,assigned_to,enquiry_date,preferred_configuration,created_by,created_at,updated_at) values (?,?,?,?,?,?,?,?,?,CURRENT_DATE,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)", l[0],l[1],l[2],l[3],l[4],l[5],l[6],l[7],l[8],l[9],l[8]);
    UUID visitLead = leadId(jdbc,"LD-2026-0002"); UUID visitedLead = leadId(jdbc,"LD-2026-0006");
    jdbc.update("insert into site_visits(lead_id,visit_date,visit_time,executive_id,status,pickup_required,feedback) values (?,CURRENT_DATE,'11:30 AM',?,'SCHEDULED',true,'Interested in B-401 amenities')",visitLead,salesExecutive);
    jdbc.update("insert into site_visits(lead_id,visit_date,visit_time,executive_id,status,pickup_required,feedback) values (?,DATEADD('DAY',-2,CURRENT_DATE),'4:00 PM',?,'VISITED',false,'Requested price breakup')",visitedLead,salesExecutive);
    UUID bookedUnit = jdbc.queryForObject("select id from units where unit_number='B-102'",UUID.class); UUID bookingLead = leadId(jdbc,"LD-2026-0004");
    jdbc.update("insert into bookings(booking_number,lead_id,unit_id,booking_amount,booking_date,status) values ('BK-2026-0001',?,?,250000,CURRENT_DATE,'CONFIRMED')",bookingLead,bookedUnit);
    UUID booking = jdbc.queryForObject("select id from bookings where booking_number='BK-2026-0001'",UUID.class);
    jdbc.update("insert into customer_payments(booking_id,receipt_number,payment_type,amount,payment_date,due_date,payment_mode,transaction_reference,status) values (?,'RC-2026-0001','BOOKING_AMOUNT',250000,CURRENT_DATE,DATEADD('DAY',10,CURRENT_DATE),'UPI','UPI-SVC-1001','PAID')",booking);
    jdbc.update("insert into customer_payments(booking_id,receipt_number,payment_type,amount,payment_date,due_date,payment_mode,transaction_reference,status) values (?,'RC-2026-0002','AGREEMENT_PAYMENT',450000,CURRENT_DATE,DATEADD('DAY',7,CURRENT_DATE),'BANK_TRANSFER','UTR-SVC-1002','PENDING')",booking);
    String[][] employees = {{"EMP-001","sales.executive@saivandan.local","Sales","Sales Executive","55000"},{"EMP-002","sales.manager@saivandan.local","Sales","Sales Manager","78000"},{"EMP-003","finance@saivandan.local","Finance","Accounts & Finance","68000"},{"EMP-004","hr@saivandan.local","Human Resources","HR & Payroll","62000"}};
    for (String[] e : employees) jdbc.update("insert into employees(employee_code,user_id,department,designation,joining_date,basic_salary,active) values (?,?,?,?,CURRENT_DATE,?,true)",e[0],userId(users,e[1]),e[2],e[3],new java.math.BigDecimal(e[4]));
    jdbc.update("insert into vendors(vendor_code,vendor_name,company_name,category,mobile,email,active) values ('VND-001','Prism Electricals','Prism Electricals Pvt Ltd','Electrical Contractor','9000000011','contact@prism.example',true)");
    jdbc.update("insert into vendors(vendor_code,vendor_name,company_name,category,mobile,email,active) values ('VND-002','StoneCraft Materials','StoneCraft Supplies','Material Supplier','9000000012','sales@stonecraft.example',true)");
    UUID vendor1 = jdbc.queryForObject("select id from vendors where vendor_code='VND-001'",UUID.class); UUID vendor2 = jdbc.queryForObject("select id from vendors where vendor_code='VND-002'",UUID.class);
    jdbc.update("insert into vendor_bills(vendor_id,invoice_number,invoice_date,amount,gst_amount,due_date,status) values (?,'PE-148',CURRENT_DATE,184500,33210,DATEADD('DAY',5,CURRENT_DATE),'PENDING')",vendor1);
    jdbc.update("insert into vendor_bills(vendor_id,invoice_number,invoice_date,amount,gst_amount,due_date,status) values (?,'SC-322',CURRENT_DATE,326000,58680,DATEADD('DAY',-2,CURRENT_DATE),'OVERDUE')",vendor2);
    UUID finance = userId(users,"finance@saivandan.local"); jdbc.update("insert into petty_cash_entries(voucher_number,entry_date,category,description,amount,payment_mode,requested_by,approved_by,status) values ('PC-2026-001',CURRENT_DATE,'Site Expenses','Site refreshments and water',2850,'CASH',?,?, 'APPROVED')",salesExecutive,finance);
    UUID support = userId(users,"support@saivandan.local"); jdbc.update("insert into support_tickets(ticket_number,booking_id,category,priority,status,subject,description,assigned_to,due_at) values ('SUP-2026-001',?,'Documentation','HIGH','OPEN','Agreement copy request','Customer requested a signed agreement copy',?,DATEADD('DAY',2,CURRENT_TIMESTAMP))",booking,support);
    seedWorkspaceRecords(jdbc, salesManager, salesExecutive, finance, userId(users,"hr@saivandan.local"), userId(users,"vendor@saivandan.local"), support);
  }
  private void seedWorkspaceRecords(JdbcTemplate jdbc, UUID manager, UUID executive, UUID finance, UUID hr, UUID vendor, UUID support) {
    Object[][] records = {
      {"user-management","Quarterly access review","PENDING","Review active users and role assignments",manager}, {"project-management","Sai Vandan Complex price list","APPROVED","Updated 2 BHK and 3 BHK pricing for Wing B",manager}, {"lead-management","April lead allocation","IN_PROGRESS","Six active leads distributed across the sales team",manager}, {"employee-management","Sales incentive roster","OPEN","Commission eligibility for confirmed bookings",hr}, {"vendor-management","Prism Electricals compliance","PENDING","GST certificate and bank verification",vendor}, {"finance","Month-end collection reconciliation","IN_PROGRESS","Reconcile UPI and bank transfer receipts",finance}, {"payroll","July payroll run","PENDING","Validate incentives, PF and ESIC inputs",hr}, {"system-configuration","Lead source master","COMPLETED","Website, referral, walk-in and portal sources",manager}, {"audit-logs","Sensitive access review","OPEN","Review financial and document access events",manager},
      {"sales-monitoring","Executive follow-up review","OPEN","Overdue follow-up review for this week",manager}, {"negotiation","B-102 discount approval","PENDING","Special offer awaiting manager decision",manager}, {"lead-qualification","Aarav Mehta qualification","COMPLETED","Budget, loan and purchase timeline captured",executive}, {"follow-ups","Riya Kapoor call back","OPEN","Confirm site visit pickup address",executive}, {"properties","B-401 availability check","OPEN","3 BHK, 980 sq ft, east facing",executive}, {"bookings","B-102 booking checklist","IN_PROGRESS","Booking payment received; documents pending",executive}, {"documents","PAN and Aadhaar collection","PENDING","Customer document verification queue",executive}, {"customers","Booked customer onboarding","OPEN","Welcome call and payment plan shared",executive},
      {"employees","New joiner onboarding","IN_PROGRESS","Collect joining documents and emergency contact",hr}, {"attendance","July attendance review","OPEN","Resolve missing check-in entries",hr}, {"leave","Leave approval queue","PENDING","Three requests await HR review",hr}, {"salary","Salary slip generation","OPEN","Prepare July salary slips",hr},
      {"customer-payments","Agreement installment RC-2026-0002","PENDING","450000 due in seven days",finance}, {"loans","Aarav home loan verification","IN_PROGRESS","Bank verification documents submitted",finance}, {"agreements","B-102 agreement registration","OPEN","Stamp duty estimate pending",finance}, {"vendor-payments","StoneCraft invoice SC-322","PENDING","Overdue vendor bill for approval",finance}, {"petty-cash","Site expense voucher PC-2026-001","APPROVED","Refreshments and water expense",finance},
      {"vendors","New plumbing contractor","OPEN","Evaluate vendor for Wing C",vendor}, {"purchase-orders","PO-2026-008 electrical material","PENDING","Await delivery confirmation",vendor}, {"vendor-bills","Invoice PE-148","PENDING","GST invoice uploaded for review",vendor}, {"vendor-ledger","Prism Electricals ledger","OPEN","Outstanding balance review",vendor},
      {"complaints","Lift service complaint","OPEN","Wing B lift inspection required",support}, {"maintenance","Flat B-102 snag visit","IN_PROGRESS","Schedule plumbing inspection",support}, {"customers","Possession readiness call","OPEN","Confirm final inspection date",support}, {"possession","B-102 key handover checklist","PENDING","Utility connection and possession letter",support}
    };
    for(Object[] r:records) jdbc.update("insert into workspace_records(module,title,status,details,created_by) values (?,?,?,?,?)",r[0],r[1],r[2],r[3],r[4]);
  }
  private UUID userId(AppUserRepository users, String email) { return users.findByEmailIgnoreCase(email).orElseThrow().getId(); }
  private UUID leadId(JdbcTemplate jdbc, String number) { return jdbc.queryForObject("select id from leads where lead_number=?", UUID.class, number); }
  private record DemoAccount(String name, String email, RoleCode role) {}
}
