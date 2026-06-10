CREATE INDEX IF NOT EXISTS idx_insured_status ON insured_persons(status);
CREATE INDEX IF NOT EXISTS idx_insured_created_at ON insured_persons(created_at);
CREATE INDEX IF NOT EXISTS idx_doctors_type_active ON doctors(type, active);
CREATE INDEX IF NOT EXISTS idx_doctors_specialty_upper ON doctors(UPPER(specialty));
