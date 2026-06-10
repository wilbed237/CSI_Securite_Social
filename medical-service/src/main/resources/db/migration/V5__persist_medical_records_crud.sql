ALTER TABLE consultations ADD COLUMN IF NOT EXISTS consultation_type VARCHAR(80) NOT NULL DEFAULT 'GENERAL';
ALTER TABLE consultations ADD COLUMN IF NOT EXISTS diagnosis VARCHAR(2000);
ALTER TABLE consultations ADD COLUMN IF NOT EXISTS conclusion VARCHAR(2000);
ALTER TABLE consultations ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE consultations ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE consultations ADD COLUMN IF NOT EXISTS updated_by_user_id UUID;
ALTER TABLE consultations ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE prescriptions ADD COLUMN IF NOT EXISTS disease_sheet_id UUID;
ALTER TABLE prescriptions ADD COLUMN IF NOT EXISTS notes VARCHAR(2000);
ALTER TABLE prescriptions ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE prescriptions ADD COLUMN IF NOT EXISTS created_by_user_id UUID;
ALTER TABLE prescriptions ADD COLUMN IF NOT EXISTS updated_by_user_id UUID;
ALTER TABLE prescriptions ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE prescriptions ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE prescriptions ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
UPDATE prescriptions p SET created_by_user_id = c.created_by_user_id FROM consultations c WHERE p.consultation_id = c.id AND p.created_by_user_id IS NULL;
ALTER TABLE prescriptions DROP CONSTRAINT IF EXISTS ck_prescription_status;
ALTER TABLE prescriptions ADD CONSTRAINT ck_prescription_status CHECK (status IN ('DRAFT','ACTIVE','FINALIZED','CANCELLED'));

ALTER TABLE medications ADD COLUMN IF NOT EXISTS administration_route VARCHAR(120);

ALTER TABLE disease_sheets ADD COLUMN IF NOT EXISTS prescription_id UUID;
ALTER TABLE disease_sheets ADD COLUMN IF NOT EXISTS patient_id VARCHAR(60);
ALTER TABLE disease_sheets ADD COLUMN IF NOT EXISTS doctor_id VARCHAR(60);
ALTER TABLE disease_sheets ADD COLUMN IF NOT EXISTS doctor_type VARCHAR(30);
ALTER TABLE disease_sheets ADD COLUMN IF NOT EXISTS specialty VARCHAR(120);
ALTER TABLE disease_sheets ADD COLUMN IF NOT EXISTS consultation_amount NUMERIC(12,2);
ALTER TABLE disease_sheets ADD COLUMN IF NOT EXISTS consultation_date TIMESTAMP;
ALTER TABLE disease_sheets ADD COLUMN IF NOT EXISTS registration_date DATE;
ALTER TABLE disease_sheets ADD COLUMN IF NOT EXISTS medical_conclusion VARCHAR(2000);
ALTER TABLE disease_sheets ADD COLUMN IF NOT EXISTS reimbursement_id UUID;
ALTER TABLE disease_sheets ADD COLUMN IF NOT EXISTS created_by_user_id UUID;
ALTER TABLE disease_sheets ADD COLUMN IF NOT EXISTS updated_by_user_id UUID;
ALTER TABLE disease_sheets ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE disease_sheets ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE disease_sheets ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

UPDATE disease_sheets ds
SET patient_id = c.insurance_number,
    doctor_id = c.doctor_matricule,
    doctor_type = c.doctor_type,
    consultation_amount = c.cost,
    consultation_date = c.started_at,
    registration_date = ds.date,
    medical_conclusion = COALESCE(c.conclusion, ds.diagnosis),
    created_by_user_id = c.created_by_user_id
FROM consultations c
WHERE ds.consultation_id = c.id;

UPDATE disease_sheets ds
SET prescription_id = (
    SELECT p.id FROM prescriptions p
    WHERE p.consultation_id = ds.consultation_id
    ORDER BY p.prescription_date DESC, p.id
    LIMIT 1
)
WHERE ds.prescription_id IS NULL;

UPDATE prescriptions p SET disease_sheet_id = ds.id
FROM disease_sheets ds
WHERE p.id = ds.prescription_id AND p.disease_sheet_id IS NULL;

ALTER TABLE disease_sheets ALTER COLUMN patient_id SET NOT NULL;
ALTER TABLE disease_sheets ALTER COLUMN doctor_id SET NOT NULL;
ALTER TABLE disease_sheets ALTER COLUMN doctor_type SET NOT NULL;
ALTER TABLE disease_sheets ALTER COLUMN consultation_amount SET NOT NULL;
ALTER TABLE disease_sheets ALTER COLUMN consultation_date SET NOT NULL;
ALTER TABLE disease_sheets ALTER COLUMN registration_date SET NOT NULL;
ALTER TABLE disease_sheets DROP CONSTRAINT IF EXISTS fk_disease_sheet_prescription;
ALTER TABLE disease_sheets ADD CONSTRAINT fk_disease_sheet_prescription FOREIGN KEY (prescription_id) REFERENCES prescriptions(id);
ALTER TABLE prescriptions DROP CONSTRAINT IF EXISTS fk_prescription_disease_sheet;
ALTER TABLE prescriptions ADD CONSTRAINT fk_prescription_disease_sheet FOREIGN KEY (disease_sheet_id) REFERENCES disease_sheets(id);

CREATE INDEX IF NOT EXISTS idx_consultations_patient ON consultations(insurance_number);
CREATE INDEX IF NOT EXISTS idx_consultations_doctor ON consultations(doctor_matricule);
CREATE INDEX IF NOT EXISTS idx_consultations_status ON consultations(status);
CREATE INDEX IF NOT EXISTS idx_consultations_created_at ON consultations(created_at);
CREATE INDEX IF NOT EXISTS idx_prescriptions_consultation ON prescriptions(consultation_id);
CREATE INDEX IF NOT EXISTS idx_prescriptions_status ON prescriptions(status);
CREATE INDEX IF NOT EXISTS idx_prescriptions_created_at ON prescriptions(created_at);
CREATE INDEX IF NOT EXISTS idx_medications_name ON medications(LOWER(name));
CREATE INDEX IF NOT EXISTS idx_disease_sheets_patient ON disease_sheets(patient_id);
CREATE INDEX IF NOT EXISTS idx_disease_sheets_doctor ON disease_sheets(doctor_id);
CREATE INDEX IF NOT EXISTS idx_disease_sheets_status ON disease_sheets(status);
CREATE INDEX IF NOT EXISTS idx_disease_sheets_consultation_date ON disease_sheets(consultation_date);
CREATE INDEX IF NOT EXISTS idx_disease_sheets_created_at ON disease_sheets(created_at);
CREATE INDEX IF NOT EXISTS idx_disease_sheets_prescription ON disease_sheets(prescription_id);
