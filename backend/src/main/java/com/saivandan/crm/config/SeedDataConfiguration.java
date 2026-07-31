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
        new DemoAccount("Sai Vandan Administrator", "admin@saivandan.local", RoleCode.SUPER_ADMIN),
        new DemoAccount("Operations Administrator", "operations.admin@saivandan.local", RoleCode.ADMIN),
        new DemoAccount("Meera Kulkarni", "management@saivandan.local", RoleCode.MANAGEMENT),
        new DemoAccount("Rahul Deshmukh", "sales.manager@saivandan.local", RoleCode.SALES_MANAGER),
        new DemoAccount("Priya Sharma", "sales.executive@saivandan.local", RoleCode.SALES_EXECUTIVE),
        new DemoAccount("Anita Patil", "telecaller@saivandan.local", RoleCode.TELECALLER),
        new DemoAccount("Kavita Jadhav", "reception@saivandan.local", RoleCode.RECEPTION),
        new DemoAccount("Vikram Joshi", "inventory@saivandan.local", RoleCode.INVENTORY_MANAGER),
        new DemoAccount("Neha Shah", "accounts.manager@saivandan.local", RoleCode.ACCOUNTS_MANAGER),
        new DemoAccount("Arjun Rao", "accounts.executive@saivandan.local", RoleCode.ACCOUNTS_EXECUTIVE),
        new DemoAccount("Pooja Nair", "hr.manager@saivandan.local", RoleCode.HR_MANAGER),
        new DemoAccount("Rohan Mehta", "hr.executive@saivandan.local", RoleCode.HR_EXECUTIVE),
        new DemoAccount("Sanjay Patil", "procurement@saivandan.local", RoleCode.PROCUREMENT_MANAGER),
        new DemoAccount("Aditi Kulkarni", "legal@saivandan.local", RoleCode.LEGAL_OFFICER),
        new DemoAccount("Sneha More", "support@saivandan.local", RoleCode.SUPPORT_EXECUTIVE),
        new DemoAccount("Internal Auditor", "auditor@saivandan.local", RoleCode.AUDITOR),
        new DemoAccount("Demo Customer", "customer@saivandan.local", RoleCode.CUSTOMER)
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
    UUID salesExecutive = userId(users,"sales.executive@saivandan.local"); UUID telecaller = userId(users,"telecaller@saivandan.local"); UUID salesManager = userId(users,"sales.manager@saivandan.local");
    Object[][] leads = {{"LD-2026-0001","Aarav Mehta","9876543210","aarav@example.com","Pune","Website","QUALIFIED","HOT",salesExecutive,"2 BHK"},{"LD-2026-0002","Riya Kapoor","9876543211","riya@example.com","Mumbai","Referral","SITE_VISIT_SCHEDULED","HOT",salesExecutive,"3 BHK"},{"LD-2026-0003","Dev Malhotra","9876543212","dev@example.com","Pune","Google Ads","NEW","WARM",telecaller,"2 BHK"},{"LD-2026-0004","Nisha Iyer","9876543213","nisha@example.com","Nashik","Walk-in","NEGOTIATION","HOT",salesManager,"3 BHK"},{"LD-2026-0005","Rohan Shah","9876543214","rohan@example.com","Pune","WhatsApp","FUTURE_PROSPECT","COLD",telecaller,"1 BHK"},{"LD-2026-0006","Isha Desai","9876543215","isha@example.com","Pune","Facebook / Instagram","VISITED","WARM",salesExecutive,"2 BHK"}};
    for (Object[] l : leads) jdbc.update("insert into leads(lead_number,customer_name,mobile,email,city,source,status,temperature,assigned_to,enquiry_date,preferred_configuration,created_by,created_at,updated_at) values (?,?,?,?,?,?,?,?,?,CURRENT_DATE,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)", l[0],l[1],l[2],l[3],l[4],l[5],l[6],l[7],l[8],l[9],l[8]);
    UUID visitLead = leadId(jdbc,"LD-2026-0002"); UUID visitedLead = leadId(jdbc,"LD-2026-0006");
    jdbc.update("insert into site_visits(lead_id,visit_date,visit_time,executive_id,status,pickup_required,feedback) values (?,CURRENT_DATE,'11:30 AM',?,'SCHEDULED',true,'Interested in B-401 amenities')",visitLead,salesExecutive);
    jdbc.update("insert into site_visits(lead_id,visit_date,visit_time,executive_id,status,pickup_required,feedback) values (?,DATEADD('DAY',-2,CURRENT_DATE),'4:00 PM',?,'VISITED',false,'Requested price breakup')",visitedLead,salesExecutive);
    UUID bookedUnit = jdbc.queryForObject("select id from units where unit_number='B-102'",UUID.class); UUID bookingLead = leadId(jdbc,"LD-2026-0004");
    jdbc.update("insert into bookings(booking_number,lead_id,unit_id,booking_amount,booking_date,status) values ('BK-2026-0001',?,?,250000,CURRENT_DATE,'CONFIRMED')",bookingLead,bookedUnit);
    UUID booking = jdbc.queryForObject("select id from bookings where booking_number='BK-2026-0001'",UUID.class);
    jdbc.update("insert into customer_payments(booking_id,receipt_number,payment_type,amount,payment_date,due_date,payment_mode,transaction_reference,status) values (?,'RC-2026-0001','BOOKING_AMOUNT',250000,CURRENT_DATE,DATEADD('DAY',10,CURRENT_DATE),'UPI','UPI-SVC-1001','PAID')",booking);
    jdbc.update("insert into customer_payments(booking_id,receipt_number,payment_type,amount,payment_date,due_date,payment_mode,transaction_reference,status) values (?,'RC-2026-0002','AGREEMENT_PAYMENT',450000,CURRENT_DATE,DATEADD('DAY',7,CURRENT_DATE),'BANK_TRANSFER','UTR-SVC-1002','PENDING')",booking);
    String[][] employees = {{"EMP-001","sales.executive@saivandan.local","Sales","Sales Executive","55000"},{"EMP-002","telecaller@saivandan.local","Sales","Telecaller","32000"},{"EMP-003","accounts.executive@saivandan.local","Finance","Accounts Executive","48000"},{"EMP-004","hr.executive@saivandan.local","Human Resources","HR Executive","42000"}};
    for (String[] e : employees) jdbc.update("insert into employees(employee_code,user_id,department,designation,joining_date,basic_salary,active) values (?,?,?,?,CURRENT_DATE,?,true)",e[0],userId(users,e[1]),e[2],e[3],new java.math.BigDecimal(e[4]));
    jdbc.update("insert into vendors(vendor_code,vendor_name,company_name,category,mobile,email,active) values ('VND-001','Prism Electricals','Prism Electricals Pvt Ltd','Electrical Contractor','9000000011','contact@prism.example',true)");
    jdbc.update("insert into vendors(vendor_code,vendor_name,company_name,category,mobile,email,active) values ('VND-002','StoneCraft Materials','StoneCraft Supplies','Material Supplier','9000000012','sales@stonecraft.example',true)");
    UUID vendor1 = jdbc.queryForObject("select id from vendors where vendor_code='VND-001'",UUID.class); UUID vendor2 = jdbc.queryForObject("select id from vendors where vendor_code='VND-002'",UUID.class);
    jdbc.update("insert into vendor_bills(vendor_id,invoice_number,invoice_date,amount,gst_amount,due_date,status) values (?,'PE-148',CURRENT_DATE,184500,33210,DATEADD('DAY',5,CURRENT_DATE),'PENDING')",vendor1);
    jdbc.update("insert into vendor_bills(vendor_id,invoice_number,invoice_date,amount,gst_amount,due_date,status) values (?,'SC-322',CURRENT_DATE,326000,58680,DATEADD('DAY',-2,CURRENT_DATE),'OVERDUE')",vendor2);
    UUID accounts = userId(users,"accounts.manager@saivandan.local"); jdbc.update("insert into petty_cash_entries(voucher_number,entry_date,category,description,amount,payment_mode,requested_by,approved_by,status) values ('PC-2026-001',CURRENT_DATE,'Site Expenses','Site refreshments and water',2850,'CASH',?,?, 'APPROVED')",salesExecutive,accounts);
    UUID support = userId(users,"support@saivandan.local"); jdbc.update("insert into support_tickets(ticket_number,booking_id,category,priority,status,subject,description,assigned_to,due_at) values ('SUP-2026-001',?,'Documentation','HIGH','OPEN','Agreement copy request','Customer requested a signed agreement copy',?,DATEADD('DAY',2,CURRENT_TIMESTAMP))",booking,support);
  }
  private UUID userId(AppUserRepository users, String email) { return users.findByEmailIgnoreCase(email).orElseThrow().getId(); }
  private UUID leadId(JdbcTemplate jdbc, String number) { return jdbc.queryForObject("select id from leads where lead_number=?", UUID.class, number); }
  private record DemoAccount(String name, String email, RoleCode role) {}
}
