CREATE TABLE roles (
  id UUID DEFAULT RANDOM_UUID() PRIMARY KEY, code VARCHAR(60) UNIQUE NOT NULL, name VARCHAR(120) NOT NULL,
  description VARCHAR(1000), created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE users (
  id UUID DEFAULT RANDOM_UUID() PRIMARY KEY, full_name VARCHAR(160) NOT NULL, email VARCHAR(180) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL, mobile VARCHAR(20), active BOOLEAN NOT NULL DEFAULT TRUE,
  last_login_at TIMESTAMP WITH TIME ZONE, created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE user_roles (user_id UUID NOT NULL REFERENCES users(id), role_id UUID NOT NULL REFERENCES roles(id), PRIMARY KEY(user_id, role_id));
CREATE TABLE leads (
  id UUID DEFAULT RANDOM_UUID() PRIMARY KEY, lead_number VARCHAR(30) UNIQUE NOT NULL, customer_name VARCHAR(160) NOT NULL, mobile VARCHAR(20) NOT NULL,
  email VARCHAR(180), city VARCHAR(100), budget_min DECIMAL(15,2), budget_max DECIMAL(15,2), preferred_configuration VARCHAR(20), source VARCHAR(50) NOT NULL,
  status VARCHAR(40) NOT NULL DEFAULT 'NEW', temperature VARCHAR(10), assigned_to UUID REFERENCES users(id), enquiry_date DATE NOT NULL,
  loan_required BOOLEAN, preferred_location VARCHAR(160), purchase_timeline VARCHAR(60), purchase_purpose VARCHAR(30), notes VARCHAR(2000), created_by UUID REFERENCES users(id),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, deleted_at TIMESTAMP WITH TIME ZONE
);
CREATE TABLE lead_activities (
  id UUID DEFAULT RANDOM_UUID() PRIMARY KEY, lead_id UUID NOT NULL REFERENCES leads(id), type VARCHAR(40) NOT NULL, outcome VARCHAR(80), remarks VARCHAR(2000),
  scheduled_at TIMESTAMP WITH TIME ZONE, completed_at TIMESTAMP WITH TIME ZONE, next_follow_up_at TIMESTAMP WITH TIME ZONE, created_by UUID REFERENCES users(id), created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_leads_mobile ON leads(mobile);
CREATE INDEX idx_leads_assigned_to ON leads(assigned_to);

INSERT INTO roles(code, name, description) VALUES
('SUPER_ADMIN','Super Admin','Complete platform administration'), ('SALES_MANAGER','Sales Manager','Sales team management'), ('SALES_EXECUTIVE','Sales Executive','Assigned customer lifecycle'),
('HR','HR & Payroll','Employees and payroll'), ('FINANCE','Accounts & Finance','Receivables and finance'), ('VENDOR','Vendor Manager','Vendors and procurement'), ('SUPPORT','Customer Support','Complaints and after-sales');
