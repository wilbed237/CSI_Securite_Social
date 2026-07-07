# Documentation détaillée du projet CSI Sécurité Sociale

## 1. Introduction

Ce projet est une application de gestion d’assurance santé et de remboursement médical, développée selon une architecture de microservices. Il combine :

- un frontend en React + TypeScript + Vite,
- un backend Java Spring Boot réparti en plusieurs services métier,
- une base de données PostgreSQL par service,
- une passerelle API (API Gateway) et un registre de services (Eureka).

L’objectif global est de proposer une plateforme complète pour gérer :

- l’authentification et les comptes utilisateurs,
- les profils d’assurés, médecins et agents sociaux,
- les consultations médicales,
- les prescriptions et orientations,
- les feuilles de maladie,
- les remboursements médicaux.

En pratique, ce projet n’est pas seulement une interface web : c’est un système complet où chaque service possède sa propre logique métier et ses propres données.

---

## 2. Vision générale du projet

Le système suit un parcours métier proche de celui d’un centre de gestion de santé :

- un assuré peut être enregistré et suivi,
- un médecin peut réaliser une consultation et produire une feuille de maladie,
- un agent social peut contrôler et valider certaines étapes,
- un administrateur peut gérer les paramètres de l’application.

Le projet est pensé pour être modulaire, extensible et facile à maintenir. La séparation des responsabilités entre services permet d’ajouter de nouvelles fonctionnalités sans casser l’ensemble du système.

---

## 3. Architecture globale

### 3.1 Vue d’ensemble

L’application suit une architecture orientée services avec un point d’entrée unique.

```text
Frontend React / Vite
        |
        v
API Gateway
        |
        +-- Auth Service
        +-- Profile Service
        +-- Medical Service
        +-- Reimbursement Service
        +-- Discovery Service (Eureka)
```

### 3.2 Composants principaux

- Frontend : interface utilisateur web.
- API Gateway : point d’entrée unique pour les clients.
- Discovery Service : annuaire permettant la découverte dynamique des services.
- Microservices backend : chacun gère un domaine métier précis.
- Bases de données : une base PostgreSQL distincte par domaine métier.

### 3.3 Pourquoi une telle architecture ?

Cette structure est utile pour :

- isoler les responsabilités métier,
- limiter les dépendances entre modules,
- faciliter la maintenance,
- permettre une évolution indépendante de chaque service.

---

## 4. Frontend

### 4.1 Technologie utilisée

Le frontend est développé avec :

- React,
- TypeScript,
- Vite,
- une logique de navigation par routes,
- des stores d’état et des hooks.

### 4.2 Rôle du frontend

Le frontend sert d’interface à l’utilisateur final. Il permet de :

- se connecter à l’application,
- naviguer entre les écrans,
- consulter les profils,
- créer ou modifier des consultations,
- gérer les prescriptions,
- suivre les feuilles de maladie,
- valider ou suivre les remboursements,
- visualiser les tableaux de bord.

### 4.3 Organisation du frontend

Le code frontend est organisé par dossiers comme :

- pages : écrans principaux,
- components : composants réutilisables,
- routes : définition de la navigation,
- hooks : logique réutilisable,
- store : état global de l’application,
- api : appels au backend,
- types : définitions TypeScript,
- layouts : structures visuelles communes.

### 4.4 Explication de code actuel du frontend

Voici quelques exemples concrets extraits du projet réel.

#### Fichier : frontend/src/App.tsx

```tsx
import { RouterProvider } from 'react-router-dom';
import { router } from './routes/AppRoutes';

export default function App() {
  return <RouterProvider router={router} />;
}
```

Ce fichier est le point d’entrée du frontend. Il charge le routeur React et affiche l’écran correspondant à l’URL courante. C’est le fichier qui “démarre” l’interface utilisateur.

#### Fichier : frontend/src/routes/AppRoutes.tsx

```tsx
export const router = createBrowserRouter([
  { path: '/', element: <LandingPage /> },
  { path: '/login', element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },
  {
    path: '/app',
    element: <ProtectedRoute><AppLayout /></ProtectedRoute>,
    children: [
      { path: 'insured', element: <InsuredListPage /> },
      { path: 'consultations', element: <ConsultationListPage /> },
    ],
  },
]);
```

Ce fichier définit l’ensemble des routes de l’application. Il permet de dire quelle page afficher selon l’URL. Par exemple, `/login` affiche la page de connexion, tandis que `/app/consultations` affiche la liste des consultations.

#### Fichier : frontend/src/routes/ProtectedRoute.tsx

```tsx
if (!accessToken) {
  return <Navigate to="/login" replace state={{ from: location }} />;
}
```

Ce code protège certaines pages. Si l’utilisateur n’est pas connecté, il est redirigé vers la page de login. Cela montre la logique de sécurité côté client.

#### Fichier : frontend/src/store/authStore.ts

```ts
export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: undefined,
      refreshToken: undefined,
      user: undefined,
      setSession: (session) => set({ accessToken: session.accessToken, refreshToken: session.refreshToken, user: session.user }),
    }),
    {
      name: 'csi-auth-session',
      storage: createJSONStorage(() => sessionStorage),
    },
  ),
);
```

Ce fichier gère la session utilisateur. Il stocke les tokens et les informations de l’utilisateur dans le navigateur pour maintenir la connexion pendant la navigation.

### 4.5 Structure réelle du frontend

Dans le dépôt, le frontend est organisé autour de dossiers tels que :

- frontend/src/pages : les vues principales,
- frontend/src/components : les composants réutilisables,
- frontend/src/routes : la navigation,
- frontend/src/store : la gestion d’état,
- frontend/src/api : les appels HTTP au backend,
- frontend/src/types : les types TypeScript.

---
---

## 5. Backend

Le backend est construit avec Spring Boot et organisé en microservices. Chaque service possède une responsabilité précise.

### 5.1 Structure commune des services Spring Boot

Les services backend suivent généralement une organisation en couches :

- application : cas d’utilisation et logique d’orchestration,
- domain : modèles métiers et règles métier,
- infrastructure : accès aux bases de données, intégrations externes,
- presentation : contrôleurs API et endpoints.

Cette structure rend le code plus lisible et facilite l’évolution du système.

### 5.2 Module commun

Le module common contient les éléments partagés par plusieurs services :

- DTO communs,
- exceptions métiers,
- classes utilitaires,
- logique de sécurité JWT commune,
- structures de réponse standard.

Ce module évite la duplication de code et rend la cohérence du système plus forte.

### 5.3 Auth Service

Ce service gère tout ce qui concerne l’authentification.

#### Responsabilités

- création de comptes utilisateurs,
- login,
- génération de JWT,
- refresh token,
- gestion des rôles et permissions.

#### Exemple de code actuel

Le fichier suivant est un bon exemple du point d’entrée du service :

Fichier : auth-service/src/main/java/com/csi/auth/AuthServiceApplication.java

```java
@SpringBootApplication(scanBasePackages = {"com.csi.auth", "com.csi.common"})
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
```

Ce code indique que le service est une application Spring Boot. L’annotation `@SpringBootApplication` permet d’activer la configuration automatique de Spring, d’enregistrer les composants du service et de démarrer l’application.

Dans ce service, la structure du code est organisée autour de packages comme :

- auth-service/src/main/java/com/csi/auth/application : logique d’utilisation,
- auth-service/src/main/java/com/csi/auth/domain : modèles et règles métier,
- auth-service/src/main/java/com/csi/auth/infrastructure : accès aux données et intégrations,
- auth-service/src/main/java/com/csi/auth/presentation : contrôleurs API.

### 5.4 Profile Service

Ce service centralise la gestion des profils métier.

#### Responsabilités

- gestion des assurés,
- gestion des médecins,
- gestion des agents sociaux,
- suivi du médecin traitant,
- définition des paramètres métier,
- statistiques de profil.

#### Exemple de structure réelle

Le service suit une organisation similaire à celle de l’auth-service :

- profile-service/src/main/java/com/csi/profile/application
- profile-service/src/main/java/com/csi/profile/domain
- profile-service/src/main/java/com/csi/profile/infrastructure
- profile-service/src/main/java/com/csi/profile/presentation

Cette organisation montre que le code est conçu selon une séparation claire entre la logique métier, les accès aux données et la couche API.

### 5.5 Medical Service

Ce service regroupe toute la logique médicale.

#### Responsabilités

- enregistrement des consultations,
- création des prescriptions,
- orientation vers un spécialiste,
- génération des feuilles de maladie,
- workflow de validation des feuilles,
- génération de documents associés.

#### Exemple de structure réelle

Le service est également organisé dans le dossier :

- medical-service/src/main/java/com/csi/medical

On y retrouve notamment les sous-packages :

- application,
- domain,
- infrastructure,
- presentation.

Cela montre que même si le domaine métier est plus complexe, la structure du code reste cohérente et facilement compréhensible.

### 5.6 Reimbursement Service

Ce service traite le flux de remboursement.

#### Responsabilités

- créer une demande de remboursement,
- calculer le montant remboursable,
- approuver ou rejeter la demande,
- exécuter le paiement,
- suivre l’état de la demande.

#### Règle métier importante

Le calcul du remboursement suit des règles prédéfinies :

- 100 % pour les consultations généralistes,
- 80 % pour les consultations spécialisées.

Cette logique métier est très importante, car elle traduit les règles de l’assurance santé dans le code.

### 5.7 API Gateway

L’API Gateway est la porte d’entrée unique de l’application.

#### Rôle

- router les requêtes vers le bon service,
- centraliser l’accès,
- simplifier l’architecture côté client,
- sécuriser les routes.

### 5.8 Discovery Service

Le service de découverte Eureka permet aux microservices de se trouver sans configuration très rigide.

---

## 6. Base de données

Le projet utilise PostgreSQL comme système de gestion de base de données relationnelle.

### 6.1 Organisation

Chaque domaine métier possède sa propre base :

- auth-db : authentification et comptes,
- profile-db : profils et paramètres,
- medical-db : consultations, prescriptions, feuilles,
- reimbursement-db : remboursements.

### 6.2 Pourquoi cette séparation ?

Cette structure offre plusieurs bénéfices :

- isolation des données métier,
- moins de couplage entre services,
- meilleure évolutivité,
- plus de clarté dans la maintenance.

### 6.3 Données principales

Les principales entités métier incluent :

- utilisateurs et rôles,
- assurés,
- médecins,
- agents sociaux,
- consultations,
- prescriptions,
- feuilles de maladie,
- remboursements,
- événements d’audit.

---

## 7. Sécurité et authentification

### 7.1 JWT

L’application utilise des tokens JWT pour sécuriser les appels API.

Le flux d’authentification repose sur :

- login,
- génération d’un access token,
- génération d’un refresh token,
- vérification du token côté API.

### 7.2 Gestion des rôles

Les rôles sont essentiels pour limiter les accès.

Exemples de rôles :

- AGENT,
- SOCIAL_AGENT,
- DOCTOR,
- GENERALIST,
- SPECIALIST,
- ADMIN.

Ces rôles conditionnent les actions possibles selon le profil utilisateur.

### 7.3 Sécurité côté frontend

Le frontend vérifie aussi certaines conditions d’accès. Par exemple, une route protégée peut être refusée si l’utilisateur n’a pas le bon rôle.

### 7.4 Données sensibles

Certaines données sensibles sont protégées, notamment :

- coordonnées bancaires,
- informations sensibles de profil,
- journaux d’audit,
- secrets d’authentification.

---

## 8. Fonctionnement métier principal

### 8.1 Inscription et création de profil

Lorsqu’un utilisateur s’inscrit, le système crée d’abord son compte dans le service d’authentification. Ensuite, un appel interne permet de créer son profil métier dans le service de profil.

Ce mécanisme montre bien la logique distribuée du projet : une action peut déclencher plusieurs services différents.

### 8.2 Gestion des assurés

Le système permet de :

- enregistrer un assuré,
- consulter ses informations,
- modifier son profil,
- attribuer ou changer un médecin traitant,
- suivre son historique.

### 8.3 Consultations médicales

Un médecin peut enregistrer une consultation liée à un assuré. Cette consultation peut ensuite entraîner :

- une prescription,
- une orientation vers un spécialiste,
- une feuille de maladie.

### 8.4 Feuilles de maladie

Le flux de feuille de maladie suit plusieurs étapes. Cela permet un contrôle et une validation progressive avant le remboursement ou le paiement.

### 8.5 Remboursements

Une fois la consultation et la feuille traitées, une demande de remboursement peut être créée puis validée ou refusée selon les règles métier.

---

## 9. Comment lire le code du projet

Pour comprendre rapidement ce projet, il faut penser en trois niveaux :

### 9.1 Le niveau métier

Il correspond aux règles du domaine :

- qui peut faire quoi,
- quelles sont les étapes d’un workflow,
- quelles conditions doivent être respectées.

### 9.2 Le niveau technique

Il correspond à la manière dont le code est organisé :

- Spring Boot pour les services backend,
- React pour le frontend,
- routes pour la navigation,
- contrôleurs pour les endpoints,
- services pour la logique métier,
- repositories pour l’accès aux données.

### 9.3 Le niveau d’intégration

Il correspond à la façon dont les services se connectent :

- le frontend appelle l’API Gateway,
- le gateway transmet la requête au bon microservice,
- les services utilisent les bases de données spécifiques à leur domaine.

### 9.4 Exemple de lecture d’un fichier précis

Si vous ouvrez le fichier suivant :

- frontend/src/routes/AppRoutes.tsx

vous pouvez comprendre que l’application est structurée autour des routes. Ce fichier indique quelles pages sont accessibles selon l’URL, par exemple `/login`, `/register` ou `/app/consultations`.

Si vous ouvrez ensuite :

- frontend/src/routes/ProtectedRoute.tsx

vous pouvez voir que certaines routes sont protégées. La logique vérifie si l’utilisateur est connecté et s’il possède les rôles nécessaires. Cela permet de comprendre comment l’accès à certaines pages est contrôlé.

Du côté backend, un exemple très clair est :

- auth-service/src/main/java/com/csi/auth/AuthServiceApplication.java

Ce fichier montre le démarrage du microservice Spring Boot. Il sert de point d’entrée et permet de comprendre que le service est lancé comme une application Java classique, mais avec les capacités de Spring Boot.

Enfin, si vous ouvrez :

- frontend/src/store/authStore.ts

vous comprenez comment la session utilisateur est conservée dans le navigateur. Ce fichier explique concrètement comment l’application “se souvient” de l’utilisateur pendant sa navigation.

---

## 10. Parcours utilisateur complet

Un parcours typique dans l’application peut être décrit ainsi :

1. L’utilisateur ouvre l’application dans le frontend.
2. Il se connecte via la page de login.
3. Le frontend envoie les informations d’authentification au backend.
4. Le service d’authentification vérifie les identifiants et renvoie un JWT.
5. Le frontend stocke ce token dans la session.
6. L’utilisateur accède à une page protégée, par exemple le tableau de bord ou la liste des consultations.
7. Selon son rôle, il peut consulter, créer ou modifier certaines données.
8. Si une consultation est créée, le système peut déclencher la logique médicale associée.
9. Une feuille de maladie peut ensuite être générée et suivie.
10. Si nécessaire, une demande de remboursement peut être créée et traitée.

Ce parcours montre bien que l’application n’est pas seulement un ensemble de pages : c’est un flux métier complet qui traverse plusieurs couches et plusieurs services.

---

## 11. Structure des dossiers du projet

Voici une structure logique du dépôt, telle qu’elle est utilisée dans le projet actuel :

- auth-service : service d’authentification
- profile-service : gestion des profils et des acteurs métier
- medical-service : consultations, prescriptions, feuilles de maladie
- reimbursement-service : remboursements
- api-gateway : point d’entrée centralisé
- discovery-service : service d’annuaire Eureka
- common : logique partagée entre services
- frontend : interface utilisateur
- docs : documentation du projet
- docker-compose.yml : orchestration des services et bases de données

Cette structure est importante, car elle reflète la séparation claire entre les domaines métier et les responsabilités techniques.

---

## 12. Déploiement et exécution

Le projet peut être exécuté localement via Docker Compose.

### 12.1 Services conteneurisés

La plateforme inclut plusieurs services Dockerisés, dont :

- discovery-service,
- auth-service,
- profile-service,
- medical-service,
- reimbursement-service,
- bases PostgreSQL associées,
- gateway.

### 12.2 Intérêt du déploiement conteneurisé

Cette approche simplifie :

- l’installation locale,
- la reproductibilité,
- la mise en place de l’environnement de développement,
- la coordination entre services.

---

## 12.3 Interaction frontend ↔ backend

Une partie importante du projet est la manière dont le frontend communique avec les services backend.

### 12.3.1 Principe général

Le frontend ne contacte pas directement chaque microservice. Il passe généralement par l’API Gateway, qui sert de point central pour les requêtes.

Cela permet de :

- centraliser les accès,
- simplifier la logique côté client,
- éviter de dupliquer les appels vers plusieurs services,
- appliquer une politique d’authentification unique.

### 12.3.2 Rôle des appels API

Dans le frontend, la logique d’appel au backend est souvent gérée dans des modules dédiés sous le dossier frontend/src/api. Ces fichiers sont responsables de :

- construire les requêtes HTTP,
- envoyer les tokens JWT,
- traiter les réponses du backend,
- transformer les données si nécessaire.

Cette séparation est importante, car elle permet de garder les composants UI plus propres et de ne pas mélanger logique métier et logique réseau.

### 12.3.3 Exemple de flux complet

Quand un utilisateur se connecte :

1. le frontend récupère ses identifiants,
2. il envoie une requête à l’API Gateway,
3. le gateway transmet la demande au service d’authentification,
4. le backend vérifie les informations et renvoie un token,
5. le frontend enregistre ce token dans la session,
6. les prochaines pages utilisent ce token pour accéder aux ressources protégées.

Ce flux illustre bien le fonctionnement réel d’une application répartie en microservices.

---

## 12.4 Configuration Docker et conteneurs

Le projet est pensé pour être lancé facilement dans un environnement local grâce à Docker Compose.

### 12.4.1 Fichier principal

Le fichier principal de configuration est :

- docker-compose.yml

Il définit :

- les services applicatifs,
- les bases de données PostgreSQL,
- les volumes de persistance,
- les ports d’exposition,
- les dépendances entre services.

### 12.4.2 Rôle des Dockerfiles

Chaque service possède son propre Dockerfile, par exemple :

- auth-service/Dockerfile
- profile-service/Dockerfile
- medical-service/Dockerfile
- reimbursement-service/Dockerfile
- api-gateway/Dockerfile

Ces fichiers permettent de construire les images du backend et de les exécuter dans des conteneurs isolés.

### 12.4.3 Intérêt

L’utilisation de Docker facilite :

- le lancement rapide du projet,
- l’isolation des services,
- la reproductibilité de l’environnement,
- l’évitement de conflits entre dépendances locales.

---

## 12.5 Fichiers de build et configuration du projet

Le dépôt contient plusieurs fichiers essentiels pour compiler et exécuter l’application.

### 12.5.1 Fichiers Maven

Le backend est principalement géré via Maven, avec des fichiers comme :

- pom.xml
- mvnw

Ces fichiers définissent :

- les dépendances Java,
- les plugins de build,
- la configuration des modules,
- les commandes de compilation et de test.

### 12.5.2 Fichiers frontend

Du côté du frontend, on retrouve par exemple :

- frontend/package.json
- frontend/vite.config.ts
- frontend/tsconfig.json

Ces fichiers servent à :

- installer les dépendances JavaScript,
- lancer le serveur de développement,
- compiler l’application pour la production,
- configurer TypeScript et Vite.

### 12.5.3 Importance de ces fichiers

Ces fichiers sont essentiels parce qu’ils permettent de transformer le code source en une application exécutable. Sans eux, le projet ne pourrait ni être compilé ni être lancé dans un environnement standard.

---

## 12.6 Contrôleurs Spring Boot

Dans les services backend, les contrôleurs sont les classes qui exposent les endpoints HTTP. Ils reçoivent les requêtes entrantes, extraient les données utiles, puis délèguent le traitement à la couche applicative.

### 12.6.1 Rôle d’un contrôleur

Un contrôleur a généralement pour mission de :

- recevoir une requête HTTP,
- valider les paramètres entrants,
- appeler la logique métier correspondante,
- retourner une réponse structurée au client.

Dans un projet Spring Boot, on retrouve souvent des annotations comme :

- `@RestController` pour indiquer qu’il s’agit d’un contrôleur API,
- `@RequestMapping` ou `@GetMapping` / `@PostMapping` pour définir les routes,
- `@RequestBody` pour lire les données JSON envoyées par le client.

### 12.6.2 Intérêt de cette séparation

Cette séparation est importante, car elle permet de garder les contrôleurs simples. Ils ne contiennent pas toute la logique métier ; ils délèguent l’exécution à des services spécialisés.

Cela rend le code plus facile à maintenir, plus lisible et plus testable.

---

## 12.7 Repositories et accès aux données

Les repositories constituent la couche d’accès aux données. Ils sont responsables de la communication avec la base de données.

### 12.7.1 Rôle d’un repository

Un repository sert généralement à :

- sauvegarder une entité,
- récupérer une ligne ou une liste d’éléments,
- effectuer des recherches avec des critères spécifiques,
- mettre à jour ou supprimer des données.

Dans une application Spring Boot, cette couche est souvent basée sur JPA ou Spring Data JPA.

### 12.7.2 Pourquoi c’est utile

Le repository masque la complexité SQL et permet au reste de l’application d’utiliser des objets métier plutôt que des requêtes brutes.

Cela apporte plusieurs avantages :

- le code métier est plus propre,
- les requêtes sont centralisées,
- les modifications de la base sont plus faciles à gérer.

### 12.7.3 Relation avec le domaine métier

La logique métier ne doit pas être écrite directement dans les repositories. Ces derniers doivent rester focalisés sur l’accès aux données. Les règles métier sont plutôt placées dans la couche service ou application.

---

## 12.8 Parcours complet d’une requête

Voici un exemple concret du comportement d’une requête dans ce projet.

### 12.8.1 Exemple : création d’une consultation

Supposons qu’un médecin crée une consultation depuis le frontend.

1. L’utilisateur remplit le formulaire dans l’interface React.
2. Le frontend envoie une requête HTTP vers l’API Gateway.
3. Le gateway redirige la requête vers le microservice médical approprié.
4. Le contrôleur du service médical reçoit la requête.
5. Le contrôleur transmet les données à la couche service.
6. La couche service applique les règles métier, par exemple :
   - vérifier que le médecin est bien autorisé,
   - vérifier que l’assuré existe,
   - vérifier que les données sont cohérentes.
7. Si tout est valide, la couche service appelle le repository.
8. Le repository sauvegarde les données dans la base PostgreSQL du service médical.
9. Une réponse est renvoyée au frontend.
10. Le frontend affiche un message ou met à jour l’interface.

Ce parcours montre bien que l’application suit une architecture en couches, où chaque partie a une responsabilité précise.

### 12.8.2 Ce qu’on peut retenir de ce flux

Ce type de flux illustre plusieurs principes fondamentaux du projet :

- séparation des responsabilités,
- logique métier centralisée,
- communication entre services,
- stockage sécurisé des données.

---

## 12.9 DTO, entités et validations

Dans les services backend, on retrouve plusieurs concepts importants qui structurent le code et le rendent plus robuste.

### 12.9.1 DTO (Data Transfer Object)

Les DTO sont des objets utilisés pour transférer des données entre les couches de l’application, notamment entre le backend et le frontend.

Leur rôle est de :

- présenter une structure claire des données envoyées ou reçues,
- éviter d’exposer directement les entités de base de données,
- contrôler ce que le client voit ou peut envoyer.

Par exemple, une requête de création d’un assuré peut contenir un DTO avec uniquement les champs nécessaires, au lieu d’envoyer toute la représentation interne de l’objet métier.

### 12.9.2 Entités JPA

Les entités représentent les données métiers stockées dans la base. Elles correspondent généralement à des tables de la base PostgreSQL.

Une entité contient souvent :

- des attributs représentant les champs métier,
- des annotations JPA pour définir la structure de mapping,
- des relations avec d’autres entités.

Par exemple, une consultation peut être liée à un assuré, à un médecin et à une feuille de maladie. Ces relations sont souvent modélisées directement dans les entités.

### 12.9.3 Validations

Les validations sont essentielles pour garantir l’intégrité des données. Elles permettent d’éviter qu’un utilisateur ou un système externe envoie des valeurs incohérentes.

On retrouve souvent :

- vérification que les champs obligatoires sont présents,
- vérification que certaines valeurs respectent un format,
- contrôle des règles métier, comme un coût positif ou une date valide.

Ces validations contribuent à améliorer la qualité du système et à éviter des erreurs coûteuses.

---

## 12.10 Tests et qualité logicielle

Un projet de cette taille doit être accompagné d’une stratégie de test sérieuse pour garantir sa stabilité.

### 12.10.1 Types de tests utiles

On peut envisager plusieurs niveaux de tests :

- tests unitaires : vérifient une petite portion de logique isolée,
- tests d’intégration : vérifient le bon fonctionnement entre plusieurs composants,
- tests API : vérifient que les endpoints répondent correctement,
- tests fonctionnels : vérifient les parcours utilisateur complets.

### 12.10.2 Importance des tests

Les tests permettent de :

- éviter les régressions,
- sécuriser les évolutions du code,
- rassurer sur la stabilité du système,
- faciliter la maintenance à long terme.

Même si le projet contient déjà une architecture solide, l’ajout de tests automatisés améliore fortement sa qualité globale.

---

## 12.11 Bonnes pratiques de développement

Pour rendre le projet plus robuste et plus maintenable, certaines bonnes pratiques sont particulièrement importantes.

### 12.11.1 Séparation claire des responsabilités

Chaque couche doit avoir un rôle précis :

- le frontend gère l’affichage,
- le backend gère la logique métier,
- la base de données stocke les informations,
- les services assurent l’orchestration.

### 12.11.2 Documentation et lisibilité

Le code devient beaucoup plus facile à maintenir quand il est bien nommé, structuré et documenté. Les noms de classes, méthodes, routes et fichiers doivent être explicites.

### 12.11.3 Sécurité par défaut

Les applications sensibles comme celle-ci doivent toujours :

- vérifier les droits d’accès,
- protéger les endpoints,
- éviter l’exposition de données sensibles,
- gérer proprement les erreurs et les exceptions.

### 12.11.4 Modularité

Le fait que le projet soit séparé en plusieurs services est un vrai avantage. Il permet d’évoluer module par module sans devoir recompiler ou réécrire l’ensemble du système.

---

## 13. Exemples de fichiers clés (contrôleurs, DTO, entités, repositories)

Pour faciliter la lecture du code, voici des exemples concrets tirés du dépôt avec leur rôle et les éléments importants à connaître.

### 13.1 Contrôleurs (endpoints)

- `auth-service/src/main/java/com/csi/auth/presentation/AuthController.java` : expose `/api/v1/auth/login`, `/api/v1/auth/refresh` et `/api/v1/auth/register`. Il utilise les DTO du package `com.csi.auth.application.dto` et délègue la logique à `AuthService`.
- `medical-service/src/main/java/com/csi/medical/presentation/ConsultationController.java` : endpoints pour créer, modifier et lister les consultations.
- `profile-service/src/main/java/com/csi/profile/presentation/InsuredController.java` : endpoints pour gérer les assurés.
- `reimbursement-service/src/main/java/com/csi/reimbursement/presentation/ReimbursementController.java` : endpoints pour calculer, approuver et exécuter les remboursements.

Ces contrôleurs sont des classes annotées `@RestController` et restent volontairement fines : validation des requêtes + appel à la couche applicative.

### 13.2 Exemple de DTO (validation)

Fichier : `auth-service/src/main/java/com/csi/auth/application/dto/AuthDtos.java`

Extrait :

```java
public record LoginRequest(@NotBlank String identifier, @NotBlank String password) {}

public record RegisterUserRequest(
  @NotBlank @Size(max = 80) String username,
  @NotBlank @Email @Size(max = 160) String email,
  @NotBlank @Size(min = 8, max = 100) String password,
  @NotEmpty Set<RoleName> roles,
  ...) {}
```

Les annotations Jakarta Validation (`@NotBlank`, `@Email`, `@Size`, `@NotEmpty`) garantissent la qualité des données côté serveur avant d’exécuter la logique métier.

### 13.3 Entités (exemples et champs importants)

- `medical-service/src/main/java/com/csi/medical/domain/model/Consultation.java`
  - champs notables : `insuranceNumber`, `doctorMatricule`, `doctorType`, `startedAt`, `endedAt`, `cost`, `status`, `idempotencyKey`, `version`.
  - `@Version` est utilisé pour la gestion de la concurrence optimiste (éviter les mises à jour concurrentes non détectées).

- `reimbursement-service/src/main/java/com/csi/reimbursement/domain/model/Reimbursement.java`
  - champs notables : `reimbursementNumber`, `sheetNumber`, `baseAmount`, `rate`, `reimbursedAmount`, `status`, `bankIbanEncrypted`, `idempotencyKey`.
  - les contraintes `@UniqueConstraint` garantissent l’unicité des numéros.

- `auth-service/src/main/java/com/csi/auth/domain/model/UserAccount.java`
  - champs notables : `username`, `email`, `passwordHash`, `roles`, `enabled`, `lastLoginAt`.

Les entités modélisent les tables PostgreSQL et contiennent souvent des hooks JPA (`@PrePersist`, `@PreUpdate`) pour initialiser des timestamps.

### 13.4 Repositories (signature et usage)

Les repositories utilisent Spring Data JPA et exposent des interfaces simples :

- `medical-service/src/main/java/com/csi/medical/infrastructure/persistence/ConsultationRepository.java`
  - `public interface ConsultationRepository extends JpaRepository<Consultation, UUID>, JpaSpecificationExecutor<Consultation>`

- `profile-service/src/main/java/com/csi/profile/infrastructure/persistence/InsuredPersonRepository.java`

Les repositories centralisent les requêtes SQL (via JPA) et s’occupent du mapping objet-relationnel.

### 13.5 Concurrence et idempotence

- `@Version` : utilisé dans `Consultation` pour empêcher l’écrasement silencieux par des mises à jour concurrentes.
- `idempotencyKey` : présent sur plusieurs entités (consultation, reimbursement) pour rendre certaines opérations idempotentes (réessayer sans créer de doublons).

### 13.6 Gestion des erreurs

- `common/src/main/java/com/csi/common/web/GlobalExceptionHandler.java` : point central pour transformer les exceptions Java en réponses API structurées.
- Chaque module peut aussi définir des `@RestControllerAdvice` (ex. `medical-service/src/main/java/com/csi/medical/presentation/MedicalSecurityExceptionHandler.java`) pour des traitements locaux d’erreurs.

---

## 14. Points forts du projet

- architecture modulaire et claire,
- séparation des responsabilités par service,
- backend robuste avec logique métier structurée,
- sécurité JWT et gestion des rôles,
- base de données séparée par domaine,
- interface web moderne et orientée utilisateur,
- prise en charge d’un workflow complet allant de la consultation jusqu’au remboursement.

---

## 15. Limites ou axes d’amélioration possibles

Selon l’évolution du projet, plusieurs améliorations pourraient être envisagées :

- ajout de tests automatisés plus complets,
- centralisation accrue de la documentation API,
- observabilité et monitoring,
- gestion des logs et traces distribuées,
- amélioration de l’interface utilisateur,
- ajout de pipelines CI/CD.

---

## 16. Conclusion

Ce projet est une solution complète de gestion de soins et de remboursements, construite autour d’une architecture microservices moderne. Il couvre à la fois les besoins fonctionnels métier et les exigences techniques d’un système distribué, avec une séparation nette entre frontend, backend, bases de données et sécurité.

Il représente un bon exemple de plateforme web d’entreprise orientée services, avec un domaine métier clair et une structure adaptable pour de futures évolutions.
