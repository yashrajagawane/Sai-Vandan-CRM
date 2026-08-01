ALTER TABLE quotation_versions ADD COLUMN IF NOT EXISTS approval_comment TEXT;
ALTER TABLE quotation_versions ADD COLUMN IF NOT EXISTS approved_by UUID REFERENCES users(id);
ALTER TABLE quotation_versions ADD COLUMN IF NOT EXISTS approved_at TIMESTAMPTZ;
CREATE TABLE IF NOT EXISTS portal_access_tokens (id UUID PRIMARY KEY DEFAULT gen_random_uuid(), customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE, token_hash VARCHAR(128) UNIQUE NOT NULL, expires_at TIMESTAMPTZ NOT NULL, revoked_at TIMESTAMPTZ, created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_portal_access_token_hash ON portal_access_tokens(token_hash);
