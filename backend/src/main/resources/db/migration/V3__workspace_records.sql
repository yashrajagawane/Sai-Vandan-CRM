CREATE TABLE workspace_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  module VARCHAR(80) NOT NULL,
  title VARCHAR(240) NOT NULL,
  status VARCHAR(40) NOT NULL DEFAULT 'OPEN',
  details VARCHAR(4000),
  created_by UUID REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_workspace_records_module ON workspace_records(module);
