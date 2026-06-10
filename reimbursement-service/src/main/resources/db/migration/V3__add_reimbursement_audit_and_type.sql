ALTER TABLE reimbursements
    ADD COLUMN reimbursement_type VARCHAR(40) NOT NULL DEFAULT 'CONSULTATION',
    ADD COLUMN processed_by_user_id UUID,
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    ADD COLUMN processed_at TIMESTAMP WITH TIME ZONE;

UPDATE reimbursements
SET processed_at = date::timestamp AT TIME ZONE 'Africa/Douala'
WHERE status = 'EXECUTED' AND processed_at IS NULL;

ALTER TABLE reimbursements
    ADD CONSTRAINT ck_reimbursement_type CHECK (reimbursement_type IN (
        'CONSULTATION','MEDICATION','HOSPITALIZATION','MEDICAL_EXAM','IMAGING','SURGERY','SPECIALIZED_CARE','OTHER'
    ));

CREATE INDEX idx_reimbursement_date_status ON reimbursements(date, status);
CREATE INDEX idx_reimbursement_agent ON reimbursements(processed_by_user_id);
CREATE INDEX idx_reimbursement_type ON reimbursements(reimbursement_type);
