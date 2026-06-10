-- Ajouter le statut COMPLETED au cycle de vie des feuilles de maladie
ALTER TABLE disease_sheets DROP CONSTRAINT IF EXISTS ck_disease_sheet_status;
ALTER TABLE disease_sheets ADD CONSTRAINT ck_disease_sheet_status CHECK
    (status IN ('DRAFT','ISSUED','SUBMITTED','UNDER_REVIEW','APPROVED','REJECTED','PAID','COMPLETED','CANCELLED'));

-- Champs pour la complétion définitive après paiement
ALTER TABLE disease_sheets ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE disease_sheets ADD COLUMN IF NOT EXISTS reimbursement_number VARCHAR(80);
ALTER TABLE disease_sheets ADD COLUMN IF NOT EXISTS completion_comment VARCHAR(1000);

-- Index pour retrouver rapidement les feuilles par numéro de remboursement
CREATE INDEX IF NOT EXISTS idx_disease_sheets_reimbursement_number ON disease_sheets(reimbursement_number) WHERE reimbursement_number IS NOT NULL;
