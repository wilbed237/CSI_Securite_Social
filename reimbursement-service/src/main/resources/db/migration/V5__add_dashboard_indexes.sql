CREATE INDEX IF NOT EXISTS idx_reimbursement_dashboard_filters ON reimbursements(date, reimbursement_type, status);
CREATE INDEX IF NOT EXISTS idx_reimbursement_recent ON reimbursements(date DESC, created_at DESC);
