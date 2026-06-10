ALTER TABLE user_accounts ADD COLUMN last_login_at TIMESTAMP WITH TIME ZONE;

CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    actor_user_id UUID,
    actor_role VARCHAR(80),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(80) NOT NULL,
    resource_id VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    result VARCHAR(30) NOT NULL,
    metadata VARCHAR(1000)
);
CREATE INDEX idx_auth_audit_timestamp ON audit_events(timestamp DESC);
