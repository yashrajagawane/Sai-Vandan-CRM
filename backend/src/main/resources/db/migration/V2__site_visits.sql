CREATE TABLE site_visits (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), lead_id UUID NOT NULL REFERENCES leads(id), visit_date DATE NOT NULL, visit_time VARCHAR(20), executive_id UUID REFERENCES users(id),
  status VARCHAR(30) NOT NULL, pickup_required BOOLEAN NOT NULL DEFAULT false, feedback TEXT, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
