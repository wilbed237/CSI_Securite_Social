INSERT INTO application_settings (id, category, setting_key, setting_value, description) VALUES
('00000000-0000-0000-0000-000000003004', 'INSURANCE', 'REIMBURSEMENT_TYPES', 'CONSULTATION,MEDICATION,HOSPITALIZATION,MEDICAL_EXAM,IMAGING,SURGERY,SPECIALIZED_CARE,OTHER', 'Types de prestations remboursables'),
('00000000-0000-0000-0000-000000001006', 'GENERAL', 'DATE_FORMAT', 'dd/MM/yyyy', 'Format de date par defaut'),
('00000000-0000-0000-0000-000000001007', 'GENERAL', 'TIME_FORMAT', 'HH:mm', 'Format d heure par defaut')
ON CONFLICT (category, setting_key) DO NOTHING;
