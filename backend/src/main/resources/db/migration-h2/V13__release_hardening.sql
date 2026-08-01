ALTER TABLE quotation_versions ADD COLUMN IF NOT EXISTS approval_comment VARCHAR(1000);
ALTER TABLE quotation_versions ADD COLUMN IF NOT EXISTS approved_by UUID REFERENCES users(id);
ALTER TABLE quotation_versions ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP WITH TIME ZONE;
CREATE TABLE portal_access_tokens (id UUID DEFAULT RANDOM_UUID() PRIMARY KEY, customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE, token_hash VARCHAR(128) UNIQUE NOT NULL, expires_at TIMESTAMP WITH TIME ZONE NOT NULL, revoked_at TIMESTAMP WITH TIME ZONE, created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE INDEX idx_portal_access_token_hash ON portal_access_tokens(token_hash);
