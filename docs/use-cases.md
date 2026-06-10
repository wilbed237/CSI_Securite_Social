# Care Health - Cas d'utilisation

## Matrice de couverture

| Identifiant | Cas | Acteur | Etat initial | Action retenue |
| --- | --- | --- | --- | --- |
| UC-AUTH-01 | Authentifier un utilisateur | Agent, medecin | Partiel | Tracer la connexion et conserver le contrat JWT |
| UC-ACCOUNT-01 | Creer un agent | Administrateur/inscription | Partiel | Rendre la synchronisation idempotente |
| UC-ACCOUNT-02 | Creer un generaliste | Administrateur/inscription | Partiel | Profil lie par `authUserId` |
| UC-ACCOUNT-03 | Creer un specialiste | Administrateur/inscription | Partiel | Specialite obligatoire |
| UC-INSURED-01 | Inscrire un assure | Agent | Present | Ajouter paiement prefere, pays et audit |
| UC-INSURED-02 | Modifier un assure | Agent | Absent | Ajouter edition et changement de statut |
| UC-INSURED-03 | Affecter le medecin traitant | Agent | Partiel | Ajouter controle actif et historique |
| UC-DOCTOR-01 | Gerer les medecins | Agent | Partiel | Ajouter changement de statut |
| UC-CONSULTATION-01 | Enregistrer une consultation | Medecin | Partiel | Ajouter ownership, motif, observations et statut |
| UC-CONSULTATION-02 | Rechercher et modifier une consultation par UUID | Medecin/agent | Absent | Pagination, filtres, verrouillage optimiste et droits proprietaire |
| UC-PRESCRIPTION-01 | Prescrire des medicaments | Medecin | Partiel | Enrichir les lignes medicament |
| UC-PRESCRIPTION-02 | Consulter et modifier une ordonnance | Medecin/agent | Absent | Lignes relationnelles, filtre medicament et verrouillage apres finalisation |
| UC-REFERRAL-01 | Orienter vers un specialiste | Generaliste | Partiel | Creer une orientation suivie et ciblee |
| UC-DISEASE-SHEET-01 | Creer une feuille | Medecin | Partiel | Ajouter contenu, workflow et audit |
| UC-DISEASE-SHEET-02 | Soumettre une feuille | Agent | Absent | Ajouter transitions controlees |
| UC-DISEASE-SHEET-03 | Completer une feuille | Agent | Absent | Ajouter paiement et reception |
| UC-DISEASE-SHEET-04 | Rechercher et corriger une feuille par UUID | Medecin/agent | Absent | Separation des champs medicaux et administratifs |
| UC-REIMBURSEMENT-01 | Choisir le paiement | Agent pour l'assure | Partiel | Conserver CASH/BANK_TRANSFER et proteger l'IBAN |
| UC-REIMBURSEMENT-02 | Calculer | Agent | Present | Configurer et historiser la regle appliquee |
| UC-REIMBURSEMENT-03 | Controler | Agent | Absent | Ajouter approbation/rejet motive |
| UC-REIMBURSEMENT-04 | Executer | Agent | Partiel | Separer creation, validation et paiement |
| UC-PRINT-01 | Generer les documents | Agent, medecin | Absent | PDF local et impression navigateur |

## Format commun

Chaque cas exige une authentification JWT, une validation Jakarta, une autorisation par role et proprietaire, une reponse DTO, une transaction locale et un audit sans donnee sensible.

Les ecrans de liste conservent `page`, `sort`, `direction`, `search` et les filtres dans l'URL. Les pages de detail et d'edition rechargent toujours la ressource depuis le backend; elles restent donc fonctionnelles apres actualisation ou collage direct de l'URL.

## Scenarios principaux

### UC-INSURED-03 - Affecter un medecin traitant

- Preconditions : agent authentifie, assure existant, generaliste actif.
- Declencheur : l'agent selectionne le generaliste.
- Nominal : fermer l'affectation courante, creer la nouvelle affectation datee, mettre a jour la vue courante, auditer.
- Erreurs : assure absent, medecin absent/inactif/specialiste.
- Postcondition : l'historique reste consultable.
- Endpoints : `POST /api/v1/insured/{insuranceNumber}/primary-doctor`, `GET /api/v1/insured/{insuranceNumber}/primary-doctor-history`.

### UC-REFERRAL-01 - Orienter vers un specialiste

- Preconditions : generaliste authentifie, consultation lui appartenant, assure actif.
- Nominal : choisir une specialite et un ou plusieurs specialistes actifs, saisir motif et priorite, creer l'orientation `PENDING`.
- Alternatives : aucun specialiste cible disponible; consultation appartenant a un autre medecin.
- Endpoints : `POST /api/v1/consultations/{id}/referrals`, `GET /api/v1/referrals`, `PATCH /api/v1/referrals/{id}/status`.

### UC-DISEASE-SHEET-02/03 - Soumettre et completer une feuille

- Preconditions : feuille emise, agent authentifie.
- Nominal : `ISSUED -> SUBMITTED -> UNDER_REVIEW`, enregistrer reception, paiement prefere et commentaire, puis approuver ou rejeter.
- Erreurs : transition interdite, feuille deja payee.
- Endpoints : `PATCH /api/v1/disease-sheets/{sheetNumber}/submit`, `PATCH /api/v1/disease-sheets/{sheetNumber}/complete`.

### UC-REIMBURSEMENT-02/03/04 - Calculer, controler et payer

- Preconditions : feuille approuvee et non deja remboursee.
- Nominal : creer la demande `PENDING`, calculer, approuver, executer le paiement, marquer la feuille `PAID`.
- Rejet : motif obligatoire, aucun paiement.
- Idempotence : une cle identique retourne l'operation existante; un second paiement est refuse.
- Endpoints : `POST /api/v1/reimbursements`, `POST /{reference}/calculate`, `PATCH /{reference}/approve`, `PATCH /{reference}/reject`, `POST /{reference}/execute`, `GET /{reference}/receipt`.
