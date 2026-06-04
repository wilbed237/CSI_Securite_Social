CREATE TABLE doctors (
    id UUID PRIMARY KEY,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    matricule VARCHAR(60) NOT NULL,
    type VARCHAR(30) NOT NULL,
    specialty VARCHAR(120),
    phone_number VARCHAR(40),
    email VARCHAR(160),
    CONSTRAINT uk_doctor_matricule UNIQUE (matricule),
    CONSTRAINT uk_doctor_email UNIQUE (email),
    CONSTRAINT ck_doctor_type CHECK (type IN ('GENERALIST','SPECIALIST')),
    CONSTRAINT ck_specialty_by_type CHECK ((type = 'SPECIALIST' AND specialty IS NOT NULL) OR (type = 'GENERALIST' AND specialty IS NULL))
);

CREATE TABLE insured_persons (
    id UUID PRIMARY KEY,
    insurance_number VARCHAR(60) NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    birth_date DATE NOT NULL,
    address VARCHAR(240) NOT NULL,
    phone_number VARCHAR(40),
    email VARCHAR(160),
    status VARCHAR(30) NOT NULL,
    treating_doctor_id UUID REFERENCES doctors(id),
    CONSTRAINT uk_insured_number UNIQUE (insurance_number),
    CONSTRAINT uk_insured_email UNIQUE (email),
    CONSTRAINT ck_insured_status CHECK (status IN ('ACTIVE','SUSPENDED'))
);
