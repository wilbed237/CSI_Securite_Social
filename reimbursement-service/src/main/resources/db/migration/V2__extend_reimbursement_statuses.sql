ALTER TABLE reimbursements DROP CONSTRAINT ck_reimbursement_status;
ALTER TABLE reimbursements ADD CONSTRAINT ck_reimbursement_status CHECK (status IN ('PENDING','EXECUTED','REJECTED'));
