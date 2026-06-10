ALTER TABLE insured_persons
    ADD COLUMN country_code VARCHAR(2) NOT NULL DEFAULT 'CM',
    ADD COLUMN preferred_payment_type VARCHAR(30) NOT NULL DEFAULT 'CASH',
    ADD COLUMN bank_account_encrypted VARCHAR(1000);

ALTER TABLE insured_persons
    ADD CONSTRAINT ck_insured_payment_type CHECK (preferred_payment_type IN ('CASH','BANK_TRANSFER'));

CREATE TABLE primary_doctor_assignments (
    id UUID PRIMARY KEY,
    insured_id UUID NOT NULL REFERENCES insured_persons(id),
    doctor_id UUID NOT NULL REFERENCES doctors(id),
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ended_at TIMESTAMP WITH TIME ZONE,
    assigned_by_user_id UUID
);
CREATE UNIQUE INDEX uk_current_primary_doctor ON primary_doctor_assignments(insured_id) WHERE ended_at IS NULL;
CREATE INDEX idx_primary_doctor_history ON primary_doctor_assignments(insured_id, started_at DESC);

INSERT INTO primary_doctor_assignments(id, insured_id, doctor_id, started_at)
SELECT gen_random_uuid(), id, treating_doctor_id, created_at
FROM insured_persons WHERE treating_doctor_id IS NOT NULL;

CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    actor_user_id UUID,
    actor_role VARCHAR(80),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(80) NOT NULL,
    resource_id VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    result VARCHAR(30) NOT NULL,
    metadata VARCHAR(1000)
);
CREATE INDEX idx_profile_audit_timestamp ON audit_events(timestamp DESC);
