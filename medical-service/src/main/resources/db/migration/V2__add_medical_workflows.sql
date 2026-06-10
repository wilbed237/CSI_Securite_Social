ALTER TABLE consultations ADD COLUMN reason VARCHAR(240) NOT NULL DEFAULT 'CONSULTATION';
ALTER TABLE consultations ADD COLUMN observations VARCHAR(2000);
ALTER TABLE consultations ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED';
ALTER TABLE consultations ADD COLUMN created_by_user_id UUID;
ALTER TABLE consultations ADD COLUMN idempotency_key VARCHAR(100);
CREATE UNIQUE INDEX uk_consultations_idempotency_key ON consultations(idempotency_key) WHERE idempotency_key IS NOT NULL;

ALTER TABLE medications ADD COLUMN frequency VARCHAR(120);
ALTER TABLE medications ADD COLUMN duration VARCHAR(120);
ALTER TABLE medications ADD COLUMN quantity INTEGER;
ALTER TABLE medications ADD COLUMN instructions VARCHAR(500);

ALTER TABLE disease_sheets DROP CONSTRAINT IF EXISTS ck_disease_sheet_status;
UPDATE disease_sheets SET status = 'ISSUED' WHERE status IN ('CREATED', 'COMPLETED');
ALTER TABLE disease_sheets ADD COLUMN received_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE disease_sheets ADD COLUMN payment_type VARCHAR(30);
ALTER TABLE disease_sheets ADD COLUMN control_comment VARCHAR(1000);
ALTER TABLE disease_sheets ADD COLUMN completed_by_user_id UUID;
ALTER TABLE disease_sheets ADD CONSTRAINT ck_disease_sheet_status CHECK
    (status IN ('DRAFT','ISSUED','SUBMITTED','UNDER_REVIEW','APPROVED','REJECTED','PAID','CANCELLED'));
ALTER TABLE disease_sheets ADD CONSTRAINT ck_disease_sheet_payment_type CHECK
    (payment_type IS NULL OR payment_type IN ('CASH','BANK_TRANSFER'));

CREATE TABLE specialist_referrals (
    id UUID PRIMARY KEY,
    referral_number VARCHAR(80) NOT NULL UNIQUE,
    consultation_id UUID NOT NULL REFERENCES consultations(id),
    specialty VARCHAR(120) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    priority VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_by_user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_referral_priority CHECK (priority IN ('ROUTINE','URGENT','EMERGENCY')),
    CONSTRAINT ck_referral_status CHECK (status IN ('PENDING','ACCEPTED','COMPLETED','CANCELLED'))
);
CREATE INDEX idx_referrals_consultation ON specialist_referrals(consultation_id);

CREATE TABLE specialist_referral_targets (
    referral_id UUID NOT NULL REFERENCES specialist_referrals(id) ON DELETE CASCADE,
    doctor_matricule VARCHAR(60) NOT NULL,
    PRIMARY KEY (referral_id, doctor_matricule)
);
CREATE INDEX idx_referral_targets_doctor ON specialist_referral_targets(doctor_matricule);

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
CREATE INDEX idx_medical_audit_timestamp ON audit_events(timestamp DESC);
