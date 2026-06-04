CREATE TABLE reimbursements (
    id UUID PRIMARY KEY,
    reimbursement_number VARCHAR(80) NOT NULL UNIQUE,
    sheet_number VARCHAR(80) NOT NULL UNIQUE,
    date DATE NOT NULL,
    payment_type VARCHAR(30) NOT NULL,
    bank_iban VARCHAR(80),
    base_amount NUMERIC(12,2) NOT NULL,
    rate NUMERIC(5,2) NOT NULL,
    reimbursed_amount NUMERIC(12,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT ck_payment_type CHECK (payment_type IN ('CASH','BANK_TRANSFER')),
    CONSTRAINT ck_reimbursement_status CHECK (status IN ('EXECUTED')),
    CONSTRAINT ck_bank_iban_for_transfer CHECK (payment_type <> 'BANK_TRANSFER' OR bank_iban IS NOT NULL)
);
