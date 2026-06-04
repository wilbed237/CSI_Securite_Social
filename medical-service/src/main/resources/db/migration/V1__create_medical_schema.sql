CREATE TABLE consultations (
    id UUID PRIMARY KEY,
    insurance_number VARCHAR(60) NOT NULL,
    doctor_matricule VARCHAR(60) NOT NULL,
    doctor_type VARCHAR(30) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP NOT NULL,
    cost NUMERIC(12,2) NOT NULL,
    CONSTRAINT ck_consultation_doctor_type CHECK (doctor_type IN ('GENERALIST','SPECIALIST')),
    CONSTRAINT ck_consultation_period CHECK (ended_at > started_at),
    CONSTRAINT ck_consultation_cost CHECK (cost >= 0)
);

CREATE TABLE prescriptions (
    id UUID PRIMARY KEY,
    prescription_number VARCHAR(60) NOT NULL UNIQUE,
    type VARCHAR(40) NOT NULL,
    prescription_date DATE NOT NULL,
    consultation_id UUID NOT NULL REFERENCES consultations(id),
    required_specialty VARCHAR(120),
    factors VARCHAR(500),
    CONSTRAINT ck_prescription_type CHECK (type IN ('MEDICATION','SPECIALIST_CONSULTATION'))
);

CREATE TABLE medications (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    posology VARCHAR(240) NOT NULL,
    prescription_id UUID NOT NULL REFERENCES prescriptions(id) ON DELETE CASCADE
);

CREATE TABLE disease_sheets (
    id UUID PRIMARY KEY,
    sheet_number VARCHAR(60) NOT NULL UNIQUE,
    date DATE NOT NULL,
    diagnosis VARCHAR(1000) NOT NULL,
    status VARCHAR(30) NOT NULL,
    consultation_id UUID NOT NULL UNIQUE REFERENCES consultations(id),
    CONSTRAINT ck_disease_sheet_status CHECK (status IN ('CREATED','COMPLETED'))
);
