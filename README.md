# CSI Backend - Microservices Spring Boot

Backend Java Spring Boot 3 / Java 17 pour le cahier de charges `csi.pdf`.

## 1. Analyse technique de `csi.pdf`

### Acteurs
- **Agent de securite sociale** : acteur principal cote organisme. Il inscrit les assures, enregistre le medecin traitant et traite les remboursements.
- **Medecin** : acteur principal. Il peut etre generaliste ou specialiste. Il enregistre les feuilles de maladie et prescrit des medicaments. Le generaliste peut orienter vers un specialiste.
- **Banque** : acteur secondaire, sollicite uniquement pour les remboursements par virement.
- **Assure / patient** : entite metier centrale mais pas acteur direct de l'application d'apres le document.

### Cas d'utilisation
1. S'authentifier.
2. Inscrire un assure.
3. Enregistrer un medecin traitant pour un assure.
4. Enregistrer une consultation.
5. Prescrire des medicaments.
6. Prescrire une consultation vers un specialiste.
7. Enregistrer une feuille de maladie.
8. Effectuer un remboursement.

### Entites metier
- Personne, Assure, Medecin, Generaliste, Specialiste.
- Consultation.
- Prescription, Prescription medicamenteuse, Prescription de consultation specialiste.
- Medicament.
- Feuille de maladie.
- Remboursement.
- Utilisateur applicatif, roles et refresh token.

### Regles metier extraites
- Toute fonctionnalite metier necessite authentification.
- Un medecin est soit generaliste, soit specialiste, jamais les deux.
- Un specialiste doit avoir une specialite ; un generaliste n'en porte pas.
- Un assure peut avoir zero ou un medecin traitant.
- Le medecin traitant doit etre un generaliste.
- Une prescription medicale necessite un patient inscrit comme assure actif.
- Seul un generaliste peut prescrire une consultation chez un specialiste.
- Une feuille de maladie documente une consultation et sert de base au remboursement.
- Une feuille de maladie ne peut etre remboursee qu'une seule fois.
- Remboursement : 100% si consultation generaliste, 80% si specialiste.
- Le paiement peut etre en especes ou par virement ; un virement exige un IBAN.

### Modules mobiles necessitant des API
- Connexion / renouvellement de session.
- Tableau de bord agent.
- Inscription et consultation des assures.
- Gestion des medecins et medecin traitant.
- Tableau de bord medecin.
- Consultations et prescriptions.
- Feuilles de maladie.
- Remboursements.

### Contraintes fonctionnelles et non fonctionnelles
- API REST versionnees `/api/v1`.
- Controle d'acces par roles.
- Validation stricte des entrees.
- Tracabilite via identifiants uniques metier.
- Separation des bases par microservice.
- Swagger/OpenAPI par service.
- Migrations Flyway.
- Docker Compose pour lancer l'architecture.

## 2. Architecture microservices cible

```text
Application mobile
      |
      v
API Gateway :8080
      |
      +--> auth-service :8081 ---- auth-db
      +--> profile-service :8082 ---- profile-db
      |       |-- assures
      |       |-- medecins
      |       |-- medecin traitant
      |
      +--> medical-service :8083 ---- medical-db
      |       |-- consultations
      |       |-- prescriptions
      |       |-- feuilles de maladie
      |       +-- REST --> profile-service (verification assure actif)
      |
      +--> reimbursement-service :8084 ---- reimbursement-db
              |-- remboursements
              +-- REST --> medical-service (feuille de maladie)
              +-- stub --> banque

Discovery service Eureka :8761
```

## 3. Microservices et responsabilites

| Service | Responsabilites |
| --- | --- |
| `discovery-service` | Registre Eureka pour decouverte des services. |
| `api-gateway` | Point d'entree unique, routage, CORS mobile. |
| `auth-service` | Utilisateurs, roles, login email/telephone/username, JWT, refresh token, BCrypt. |
| `profile-service` | Assures, medecins, specialites, medecin traitant, statut assure. |
| `medical-service` | Consultations, prescriptions medicamenteuses, prescriptions specialistes, feuilles de maladie. |
| `reimbursement-service` | Calcul et execution des remboursements, simulation banque. |

## 4. Modeles de donnees par service

### auth-service
- `user_accounts(id, username, email, phone_number, password_hash, enabled, created_at)`
- `user_roles(user_id, role)`
- `refresh_tokens(id, user_id, token, expires_at, revoked)`

### profile-service
- `doctors(id, first_name, last_name, matricule, type, specialty, phone_number, email)`
- `insured_persons(id, insurance_number, first_name, last_name, birth_date, address, phone_number, email, status, treating_doctor_id)`

### medical-service
- `consultations(id, insurance_number, doctor_matricule, doctor_type, started_at, ended_at, cost)`
- `prescriptions(id, prescription_number, type, prescription_date, consultation_id, required_specialty, factors)`
- `medications(id, name, posology, prescription_id)`
- `disease_sheets(id, sheet_number, date, diagnosis, status, consultation_id)`

### reimbursement-service
- `reimbursements(id, reimbursement_number, sheet_number, date, payment_type, bank_iban, base_amount, rate, reimbursed_amount, status)`

## 5. Endpoints REST principaux

### Auth
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/register`

### Profile
- `POST /api/v1/insured`
- `GET /api/v1/insured/{insuranceNumber}`
- `GET /api/v1/insured/{insuranceNumber}/status`
- `PUT /api/v1/insured/{insuranceNumber}/treating-doctor`
- `POST /api/v1/doctors`
- `GET /api/v1/doctors/{matricule}`
- `GET /api/v1/doctors?type=GENERALIST&page=0&size=20&sort=lastName,asc`

### Medical
- `POST /api/v1/consultations`
- `POST /api/v1/prescriptions/medications`
- `POST /api/v1/prescriptions/specialist-consultations`
- `POST /api/v1/disease-sheets`
- `GET /api/v1/disease-sheets/{sheetNumber}`

### Reimbursement
- `POST /api/v1/reimbursements`
- `GET /api/v1/reimbursements/{reimbursementNumber}`

## 6. Securite

- JWT HMAC signe par `JWT_SECRET`.
- Access token + refresh token.
- BCrypt pour les mots de passe.
- Roles : `AGENT`, `DOCTOR`, `GENERALIST`, `SPECIALIST`, `ADMIN`.
- CORS ouvert par defaut pour faciliter le developpement mobile ; a restreindre en production.
- Aucune donnee sensible en dur requise en production : utiliser les variables d'environnement.

Comptes de test crees au demarrage par `auth-service` :

| Utilisateur | Mot de passe | Roles |
| --- | --- | --- |
| `agent.csi` | `Password123!` | `AGENT`, `ADMIN` |
| `dr.generaliste` | `Password123!` | `DOCTOR`, `GENERALIST` |
| `dr.specialiste` | `Password123!` | `DOCTOR`, `SPECIALIST` |

## 7. Lancement

### Compiler

```bash
./mvnw clean package
```

Le wrapper telecharge Maven 3.9.9 dans `.mvn/` si Maven n'est pas installe globalement.

### Lancer toute l'architecture

```bash
./mvnw clean package -DskipTests
docker compose up --build
```

Services :
- Gateway : <http://localhost:8080>
- Eureka : <http://localhost:8761>
- Swagger auth : <http://localhost:8081/swagger-ui.html>
- Swagger profile : <http://localhost:8082/swagger-ui.html>
- Swagger medical : <http://localhost:8083/swagger-ui.html>
- Swagger reimbursement : <http://localhost:8084/swagger-ui.html>

### Exemple login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"identifier":"agent.csi","password":"Password123!"}'
```

## 8. Donnees de test

`profile-service` insere :
- generaliste `MED-GEN-001` ;
- specialiste `MED-SPE-001` ;
- assure actif `ASS-0001` avec medecin traitant `MED-GEN-001`.

## 9. Tests

```bash
./mvnw test
```

Tests inclus :
- generation JWT ;
- regles de profil medecin ;
- validation periode consultation ;
- calcul remboursement 100% / 80%.

## 10. Hypotheses et limites

- Le PDF decrit une application d'organisme de securite sociale mais ne fournit pas de maquettes mobiles detaillees ; les modules mobiles sont deduits des cas d'utilisation UML.
- Le document ne mentionne pas de notifications ni pieces jointes ; aucun microservice notification/document n'a ete ajoute pour eviter une fonctionnalite artificielle.
- La banque est simulee par un client applicatif (`BankPaymentClient`) qui journalise le virement sans integration externe reelle.
- Les communications interservices REST propagent le token utilisateur courant afin de conserver les controles de role.
