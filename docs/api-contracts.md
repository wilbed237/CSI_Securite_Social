# Care Health - Contrats API

Toutes les routes protegees utilisent `Authorization: Bearer <token>` et retournent `ApiResponse<T>`.

## Profils

- `POST /api/v1/insured`
- `GET /api/v1/insured?status=&search=&page=&size=`
- `GET /api/v1/insured/{insuranceNumber}`
- `PUT /api/v1/insured/{insuranceNumber}`
- `PATCH /api/v1/insured/{insuranceNumber}/status`
- `POST /api/v1/insured/{insuranceNumber}/primary-doctor`
- `GET /api/v1/insured/{insuranceNumber}/primary-doctor-history`
- `GET /api/v1/doctors?type=&active=&specialty=`
- `PATCH /api/v1/doctors/{matricule}/status`

## Medical

- `POST /api/v1/consultations`
- `GET /api/v1/consultations?page=&size=&sort=&direction=&search=&patientId=&doctorId=&doctorType=&status=&startDate=&endDate=`
- `GET /api/v1/consultations/{id}`
- `PUT|PATCH /api/v1/consultations/{id}`
- `PATCH /api/v1/consultations/{id}/cancel`
- `PATCH /api/v1/consultations/{id}/archive`
- `GET /api/v1/consultations/{id}/prescriptions`
- `GET /api/v1/consultations/{id}/disease-sheet`
- `POST /api/v1/prescriptions`
- `GET /api/v1/prescriptions?page=&size=&sort=&direction=&search=&patientId=&doctorId=&consultationId=&diseaseSheetId=&status=&startDate=&endDate=&medicationName=`
- `GET /api/v1/prescriptions/{id}`
- `PUT|PATCH /api/v1/prescriptions/{id}`
- `POST /api/v1/consultations/{id}/referrals`
- `GET /api/v1/referrals`
- `PATCH /api/v1/referrals/{id}/status`
- `POST /api/v1/disease-sheets`
- `GET /api/v1/disease-sheets?page=&size=&sort=&direction=&search=&patientId=&doctorId=&doctorType=&status=&hasReimbursement=&startDate=&endDate=`
- `GET /api/v1/disease-sheets/{id-ou-numero}`
- `PUT|PATCH /api/v1/disease-sheets/{id}`
- `GET /api/v1/disease-sheets/{id}/prescription`
- `GET /api/v1/patients/{patientId}/disease-sheets`
- `GET /api/v1/doctors/{doctorId}/disease-sheets`
- `PATCH /api/v1/disease-sheets/{sheetNumber}/submit`
- `PATCH /api/v1/disease-sheets/{sheetNumber}/complete`
- `GET /api/v1/disease-sheets/{sheetNumber}/pdf`

## Remboursements

- `POST/GET /api/v1/reimbursements`
- `GET /api/v1/reimbursements/{reference}`
- `POST /api/v1/reimbursements/{reference}/calculate`
- `PATCH /api/v1/reimbursements/{reference}/approve`
- `PATCH /api/v1/reimbursements/{reference}/reject`
- `POST /api/v1/reimbursements/{reference}/execute`
- `GET /api/v1/reimbursements/{reference}/receipt`

Les reponses ne contiennent jamais le mot de passe, les tokens, le secret JWT ou un IBAN complet.

Les mises a jour portent obligatoirement le champ `version`. Une version obsolete retourne `409 VERSION_CONFLICT`.
Les recherches acceptent un UUID complet dans `search`; une ressource absente retourne un message metier `404`, jamais une erreur SQL brute.
