<<<<<<< HEAD
# Care Health - Microservices Spring Boot + React

Application Care Health pour la gestion d'assurance sante : authentification JWT, profils assures/medecins/agents, consultations, prescriptions, feuilles de maladie, remboursements, dashboards et parametres metier.

## Architecture

```text
frontend React/Vite :5173
        |
        v
api-gateway :8080
        |
        +-- auth-service :8081 ------------ auth-db
        +-- profile-service :8082 --------- profile-db
        +-- medical-service :8083 --------- medical-db
        +-- reimbursement-service :8084 --- reimbursement-db
        +-- discovery-service :8761
```

Modules Maven :
- `common` : exceptions, DTO communs, securite JWT partagee.
- `auth-service` : comptes, roles, login, refresh tokens, JWT.
- `profile-service` : assures, medecins, agents sociaux, parametres, statistiques profils.
- `medical-service` : consultations, prescriptions, feuilles de maladie, dashboard medecin.
- `reimbursement-service` : remboursements et statistiques remboursements.
- `api-gateway` : routage `/api/v1/**` vers les microservices.
- `discovery-service` : Eureka.
- `frontend` : React, TypeScript, Vite.

## Fonctionnalites

- Login avec `identifier`, mot de passe, access token et refresh token.
- Creation de compte avec synchronisation automatique du profil metier.
- Gestion des assures, medecins generalistes, medecins specialistes et agents sociaux.
- Gestion du medecin traitant avec historique des affectations.
- Consultations idempotentes, prescriptions structurees, orientations vers des specialistes actifs et feuilles de maladie imprimables.
- Cycle des feuilles : `ISSUED`, `SUBMITTED`, `UNDER_REVIEW`, puis validation ou rejet.
- Remboursements calcules cote serveur avec statuts `PENDING`, `APPROVED`, `EXECUTED`, `REJECTED`.
- Taux centralises : generaliste 100 %, specialiste 80 %.
- Coordonnees bancaires chiffrees au repos et masquees dans les reponses API.
- Journal d'audit des operations sensibles de profil et des actes medicaux.
- Dashboard agent social avec statistiques patients, medecins, remboursements et activites recentes.
- Dashboard medecin avec consultations, patients, feuilles maladie, prescriptions et actions rapides.
- Module Parametres avec categories generales, medicales, assurance, securite et preferences UI.

## Roles

Roles supportes :
- `AGENT`
- `SOCIAL_AGENT`
- `AGENT_SOCIAL`
- `SECURITY_AGENT`
- `DOCTOR`
- `GENERALIST`
- `SPECIALIST`
- `ADMIN`

Les alias agent sont conserves pour accepter plusieurs libelles cote inscription. Les roles `GENERALIST` et `SPECIALIST` impliquent le role applicatif `DOCTOR`.

## Creation de Compte et Synchronisation Metier

Endpoint :

```text
POST /api/v1/auth/register
```

Le compte est cree dans `auth-service`, puis `auth-service` appelle `profile-service` via REST interne :

```text
POST /api/v1/internal/profiles/actors
Header: X-Internal-Secret
```

Exemple agent social :

```json
{
  "username": "agent.social",
  "email": "agent.social@carehealth.local",
  "phoneNumber": "+237690000002",
  "password": "Password123!",
  "roles": ["AGENT"],
  "actorType": "SOCIAL_AGENT",
  "firstName": "Paul",
  "lastName": "Essomba"
}
```

Exemple medecin generaliste :

```json
{
  "username": "dr.kamga",
  "email": "dr.kamga@carehealth.local",
  "phoneNumber": "+237690000000",
  "password": "Password123!",
  "roles": ["DOCTOR"],
  "actorType": "DOCTOR",
  "doctorType": "GENERALISTE",
  "firstName": "Jean",
  "lastName": "Kamga"
}
```

Exemple specialiste :

```json
{
  "username": "dr.cardio",
  "email": "cardio@carehealth.local",
  "phoneNumber": "+237690000001",
  "password": "Password123!",
  "roles": ["DOCTOR"],
  "actorType": "DOCTOR",
  "doctorType": "SPECIALISTE",
  "specialty": "CARDIOLOGIE",
  "firstName": "Alice",
  "lastName": "Mballa"
}
```

Regles :
- un agent/admin cree un profil `social_agents` lie par `auth_user_id` ;
- un generaliste cree un profil `doctors` de type `GENERALIST` ;
- un specialiste cree un profil `doctors` de type `SPECIALIST` avec specialite obligatoire ;
- les profils crees sont visibles immediatement dans les listes et statistiques de `profile-service`.

## Endpoints Principaux

Auth :
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/register`

Profile :
- `POST /api/v1/insured`
- `GET /api/v1/insured/{insuranceNumber}`
- `GET /api/v1/insured/{insuranceNumber}/status`
- `PUT /api/v1/insured/{insuranceNumber}/treating-doctor`
- `PUT /api/v1/insured/{insuranceNumber}`
- `PATCH /api/v1/insured/{insuranceNumber}/status`
- `POST /api/v1/insured/{insuranceNumber}/primary-doctor`
- `GET /api/v1/insured/{insuranceNumber}/primary-doctor-history`
- `POST /api/v1/doctors`
- `GET /api/v1/doctors/{matricule}`
- `GET /api/v1/doctors`
- `GET /api/v1/agents`

Dashboards :
- `GET /api/v1/dashboard/agent-social/summary`
- `GET /api/v1/dashboard/agent-social/patients-stats`
- `GET /api/v1/dashboard/agent-social/doctors-stats`
- `GET /api/v1/dashboard/agent-social/reimbursements-stats`
- `GET /api/v1/dashboard/agent-social/recent-activities`
- `GET /api/v1/dashboard/doctor/summary`
- `GET /api/v1/dashboard/doctor/patients-stats`
- `GET /api/v1/dashboard/doctor/consultations-stats`
- `GET /api/v1/dashboard/doctor/disease-sheets-stats`
- `GET /api/v1/dashboard/doctor/recommendations-stats`
- `GET /api/v1/dashboard/doctor/recent-activities`

Parametres :
- `GET /api/v1/settings`
- `GET /api/v1/settings/{category}`
- `PUT /api/v1/settings/{category}`

Medical :
- `POST /api/v1/consultations`
- `POST /api/v1/prescriptions/medications`
- `POST /api/v1/prescriptions/specialist-consultations`
- `POST /api/v1/consultations/{consultationId}/referrals`
- `GET /api/v1/referrals/{referralNumber}`
- `PATCH /api/v1/referrals/{referralNumber}/status`
- `POST /api/v1/disease-sheets`
- `GET /api/v1/disease-sheets/{sheetNumber}`
- `PATCH /api/v1/disease-sheets/{sheetNumber}/submit`
- `PATCH /api/v1/disease-sheets/{sheetNumber}/complete`
- `GET /api/v1/disease-sheets/{sheetNumber}/pdf`

Remboursement :
- `POST /api/v1/reimbursements`
- `GET /api/v1/reimbursements/{reimbursementNumber}`
- `POST /api/v1/reimbursements/{reimbursementNumber}/calculate`
- `PATCH /api/v1/reimbursements/{reimbursementNumber}/approve`
- `PATCH /api/v1/reimbursements/{reimbursementNumber}/reject`
- `POST /api/v1/reimbursements/{reimbursementNumber}/execute`

## Tables Principales

`auth-service` :
- `user_accounts`
- `user_roles`
- `refresh_tokens`

`profile-service` :
- `insured_persons`
- `doctors`
- `social_agents`
- `application_settings`
- `primary_doctor_assignments`
- `audit_events`

`medical-service` :
- `consultations`
- `prescriptions`
- `medications`
- `disease_sheets`
- `specialist_referrals`
- `specialist_referral_targets`
- `audit_events`

`reimbursement-service` :
- `reimbursements`

## Frontend

Le frontend consomme `VITE_API_BASE_URL` et conserve la forme de reponse existante :

```text
response.data.data.accessToken
response.data.data.refreshToken
response.data.data.user
```

Routes principales :
- `/login`
- `/register`
- `/app/dashboard`
- `/app/insured`
- `/app/doctors`
- `/app/consultations`
- `/app/prescriptions`
- `/app/disease-sheets`
- `/app/reimbursements`
- `/app/settings`

Le dashboard choisit automatiquement la vue agent social ou medecin selon les roles. Si un utilisateur possede des roles agent et medecin, un basculement de vue est affiche.

Les preferences UI suivantes sont locales au navigateur :
- theme clair ;
- sombre bleute ;
- sombre orange ;
- sombre violet ;
- sombre noir ;
- densite confortable/compacte ;
- animations normales/reduites.

Les parametres metier sont stockes dans `profile-service`.

## Variables d'Environnement

Backend :
- `JWT_SECRET`
- `PROFILE_SERVICE_URL`
- `INTERNAL_SERVICE_SECRET`
- `SENSITIVE_DATA_KEY`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Frontend :
- `VITE_API_BASE_URL=http://localhost:8080`
- `VITE_APP_NAME=Care Health`
- `VITE_APP_VERSION=1.0.0`

Docker Compose configure `PROFILE_SERVICE_URL=http://profile-service:8082` pour `auth-service` et partage `INTERNAL_SERVICE_SECRET` entre `auth-service` et `profile-service`.

## Comptes de Test

| Utilisateur | Mot de passe | Roles |
| --- | --- | --- |
| `agent.csi` | `Password123!` | `AGENT`, `ADMIN` |
| `dr.generaliste` | `Password123!` | `DOCTOR`, `GENERALIST` |
| `dr.specialiste` | `Password123!` | `DOCTOR`, `SPECIALIST` |

Exemple login :

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"identifier":"agent.csi","password":"Password123!"}'
```

## Lancement

Backend local :

```bash
./mvnw clean package -DskipTests
```

Docker :

```bash
docker compose build
docker compose up -d
```

Frontend :

```bash
cd frontend
npm install
npm run build
npm run dev
```

URLs :
- Gateway : <http://localhost:8080>
- Frontend : <http://localhost:5173>
- Eureka : <http://localhost:8761>
- Swagger auth : <http://localhost:8081/swagger-ui.html>
- Swagger profile : <http://localhost:8082/swagger-ui.html>
- Swagger medical : <http://localhost:8083/swagger-ui.html>
- Swagger reimbursement : <http://localhost:8084/swagger-ui.html>

## CRUD des documents medicaux

Les consultations, ordonnances et feuilles de maladie sont stockees dans PostgreSQL et exposees par UUID avec pagination, recherche, filtres, tri et verrouillage optimiste. Les routes frontend sont :

- `/app/consultations`, `/app/consultations/:id`, `/app/consultations/:id/edit`
- `/app/prescriptions`, `/app/prescriptions/:id`, `/app/prescriptions/:id/edit`
- `/app/disease-sheets`, `/app/disease-sheets/:id`, `/app/disease-sheets/:id/edit`

Les listes conservent leurs criteres dans la query string. Le bouton de copie copie toujours l'identifiant complet, meme lorsqu'il est tronque visuellement.

Exemples (remplacer `$TOKEN`, les UUID et les identifiants externes) :

```bash
curl -X POST http://localhost:8080/api/v1/consultations -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"insuranceNumber":"ASS-0001","startedAt":"2026-06-10T09:00:00","endedAt":"2026-06-10T09:30:00","cost":10000,"reason":"Controle"}'
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/consultations/CONSULTATION_UUID
curl -X PUT http://localhost:8080/api/v1/consultations/CONSULTATION_UUID -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"diagnosis":"Diagnostic corrige","version":0}'
curl -H "Authorization: Bearer $TOKEN" 'http://localhost:8080/api/v1/consultations?page=0&size=20&sort=startedAt&direction=desc&status=COMPLETED'

curl -X POST http://localhost:8080/api/v1/prescriptions -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"consultationId":"CONSULTATION_UUID","medications":[{"name":"Paracetamol","posology":"1 comprime","frequency":"3 fois/jour"},{"name":"Vitamine C","posology":"1 comprime/jour"}]}'
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/prescriptions/PRESCRIPTION_UUID
curl -X PUT http://localhost:8080/api/v1/prescriptions/PRESCRIPTION_UUID -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"notes":"Apres repas","version":0}'

curl -X POST http://localhost:8080/api/v1/disease-sheets -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"consultationId":"CONSULTATION_UUID","prescriptionId":"PRESCRIPTION_UUID","diagnosis":"Diagnostic"}'
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/disease-sheets/DISEASE_SHEET_UUID
curl -X PUT http://localhost:8080/api/v1/disease-sheets/DISEASE_SHEET_UUID -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"medicalConclusion":"Conclusion corrigee","version":0}'
```

## Tests

```bash
./mvnw test
cd frontend && npm run build
```

Tests backend existants :
- generation JWT ;
- regles de profil medecin ;
- validation periode consultation ;
- calcul remboursement.

## Hypotheses et Limites

- La synchronisation metier utilise un appel REST interne simple, car aucun broker evenementiel n'existait dans le projet.
- Les dashboards utilisent les donnees persistantes disponibles. Les indicateurs dont le modele ne porte pas encore les timestamps ou statuts fins retournent une valeur neutre documentee, par exemple le delai moyen de traitement.
- Les comptes de test medecins historiques ne portent pas encore un lien `auth_user_id` vers les medecins seedes ; les nouveaux comptes crees via inscription sont lies automatiquement.
- Le client banque reste simule.
- Java 17 est requis pour Spring Boot 3.
=======
# CSI_Securite_Social
Projet de conception et de création d'une application de sécurité sociale axée Médecine
>>>>>>> 4c92afcd58630728fdcbaf0ada5c927c173328c0
