# Frontend CSI Assurance Santé

Interface React moderne pour le backend CSI Spring Boot microservices. Elle couvre les cas d'utilisation du document `csi.pdf` : authentification, gestion des assurés, médecins, consultations, prescriptions, feuilles de maladie et remboursements.

## 1. Technologies utilisées

- React + Vite
- TypeScript strict
- React Router
- Axios
- TanStack Query
- React Hook Form
- Zod
- Tailwind CSS v4
- Zustand
- Lucide React
- React Hot Toast

## 2. Identité visuelle

- Mode clair / sombre avec bouton de bascule et persistance locale.

- Primaire : bleu santé professionnel `#0f4c81`
- Secondaire : cyan doux `#e0f2fe`
- Accent : vert action `#10b981`
- Neutres : palette Slate
- États : succès vert, erreur rouge, avertissement ambre, information bleu

## 3. Structure du projet

```text
frontend/
  src/api          # clients Axios et services REST
  src/components   # composants UI réutilisables
  src/config       # variables de configuration
  src/hooks        # hooks personnalisés
  src/layouts      # layouts auth et application
  src/pages        # pages métier
  src/routes       # configuration routes et menus
  src/store        # état global authentification
  src/types        # interfaces TypeScript
  src/utils        # helpers
```

## 4. Prérequis

- Node.js 20+ recommandé
- npm 10+
- Backend CSI lancé via Docker Compose ou services Spring Boot

## 5. Installation

```bash
cd frontend
npm install
```

## 6. Configuration `.env`

Créer `frontend/.env` à partir de l'exemple :

```bash
cp .env.example .env
```

Variables :

```env
VITE_API_GATEWAY_URL=http://localhost:8080
VITE_APP_NAME=CSI Assurance Santé
```

Le frontend utilise prioritairement l'API Gateway du backend.

## 7. Lancer en développement

```bash
npm run dev
```

Application disponible par défaut sur <http://localhost:5173>.

## 8. Lancer le backend nécessaire

Depuis la racine du dépôt :

```bash
docker compose up --build
```

Les images Docker compilent les services avec Maven + JDK 17. Si tu veux compiler localement avant Docker, installe un JDK 17 puis lance `./mvnw clean package -DskipTests`.

Services utiles :

- Gateway : <http://localhost:8080>
- Swagger auth : <http://localhost:8081/swagger-ui.html>
- Swagger profile : <http://localhost:8082/swagger-ui.html>
- Swagger medical : <http://localhost:8083/swagger-ui.html>
- Swagger reimbursement : <http://localhost:8084/swagger-ui.html>

## 9. Docker

Le frontend n'a pas encore de conteneur dédié dans le `docker-compose.yml` racine. Pour lancer toute l'application aujourd'hui :

```bash
# terminal 1, racine du dépôt
./mvnw clean package -DskipTests
docker compose up --build

# terminal 2
cd frontend
npm install
npm run dev
```

## 10. Routes principales

| Route | Description | Rôles |
| --- | --- | --- |
| `/` | Landing page | public |
| `/login` | Connexion | public |
| `/register` | Création compte applicatif | public |
| `/app` | Dashboard | authentifié |
| `/app/insured` | Recherche assurés | agent, médecin |
| `/app/insured/new` | Inscription assuré | agent |
| `/app/doctors` | Liste médecins | agent, médecin |
| `/app/doctors/new` | Création médecin | agent |
| `/app/consultations/new` | Création consultation | médecin |
| `/app/prescriptions` | Prescriptions | médecin |
| `/app/disease-sheets` | Feuilles maladie | agent, médecin |
| `/app/reimbursements` | Remboursements | agent |
| `/forbidden` | Accès refusé | public |

## 11. Connexion au backend

- `src/api/httpClient.ts` crée l'instance Axios.
- Le token JWT est ajouté automatiquement dans `Authorization: Bearer ...`.
- Le refresh token est utilisé automatiquement en cas de `401` si disponible.
- Les erreurs `401`, `403`, `404`, `500` sont centralisées et affichées via toasts.
- Les tokens sont stockés en `sessionStorage` via Zustand. Pour une production sensible, préférer des cookies HttpOnly côté backend.

## 12. Comptes de test backend

| Utilisateur | Mot de passe | Rôles |
| --- | --- | --- |
| `agent.csi` | `Password123!` | `AGENT`, `ADMIN` |
| `dr.generaliste` | `Password123!` | `DOCTOR`, `GENERALIST` |
| `dr.specialiste` | `Password123!` | `DOCTOR`, `SPECIALIST` |

Données utiles :

- Assuré : `ASS-0001`
- Généraliste : `MED-GEN-001`
- Spécialiste : `MED-SPE-001`

## 13. Commandes utiles

```bash
npm run dev
npm run build
npm run preview
npm run lint
npm run test
```

## 14. Problèmes fréquents

### Erreur réseau ou 500
Vérifier que le backend est lancé et que `VITE_API_GATEWAY_URL` pointe vers `http://localhost:8080`.

### 403 accès refusé
Le compte connecté n'a pas le rôle attendu. Exemple : les remboursements nécessitent `AGENT`.

### 401 session expirée
Le frontend tente un refresh token. Si le refresh échoue, reconnectez-vous.

### Création feuille maladie impossible
Créer d'abord une consultation et copier son `id` dans le formulaire feuille maladie.

### Remboursement impossible
Une feuille doit exister et ne peut être remboursée qu'une seule fois.
