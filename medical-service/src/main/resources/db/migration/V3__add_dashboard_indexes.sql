CREATE INDEX IF NOT EXISTS idx_consultations_started_at ON consultations(started_at);
CREATE INDEX IF NOT EXISTS idx_consultations_doctor_started_at ON consultations(UPPER(doctor_matricule), started_at);
CREATE INDEX IF NOT EXISTS idx_consultations_doctor_type_started_at ON consultations(doctor_type, started_at);
CREATE INDEX IF NOT EXISTS idx_disease_sheets_date ON disease_sheets(date);
CREATE INDEX IF NOT EXISTS idx_prescriptions_type ON prescriptions(type);
