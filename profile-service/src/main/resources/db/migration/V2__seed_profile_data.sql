INSERT INTO doctors (id, first_name, last_name, matricule, type, specialty, phone_number, email) VALUES
('00000000-0000-0000-0000-000000000101', 'Awa', 'Diop', 'MED-GEN-001', 'GENERALIST', NULL, '+221770000101', 'awa.diop@csi.local'),
('00000000-0000-0000-0000-000000000102', 'Moussa', 'Ndiaye', 'MED-SPE-001', 'SPECIALIST', 'Cardiologie', '+221770000102', 'moussa.ndiaye@csi.local');

INSERT INTO insured_persons (id, insurance_number, first_name, last_name, birth_date, address, phone_number, email, status, treating_doctor_id) VALUES
('00000000-0000-0000-0000-000000000201', 'ASS-0001', 'Fatou', 'Sarr', '1990-05-20', 'Dakar Plateau', '+221770000201', 'fatou.sarr@example.com', 'ACTIVE', '00000000-0000-0000-0000-000000000101');
