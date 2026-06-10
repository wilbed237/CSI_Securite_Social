# Care Health - Modele de donnees medical

La base `csi_medical` appartient exclusivement a `medical-service`. Les references vers les autres microservices restent des identifiants externes (`insurance_number`, `doctor_matricule`, `reimbursement_id`/`reimbursement_number`).

## Tables principales

- `consultations`: UUID, patient, medecin, type/date/periode, motif, observations, diagnostic, conclusion, montant, statut, audit et version.
- `prescriptions`: UUID, numero metier, consultation, feuille optionnelle, date, notes, statut, audit et version.
- `medications`: lignes relationnelles d'une ordonnance avec nom, posologie, frequence, duree, quantite, voie et instructions.
- `disease_sheets`: UUID, numero metier, consultation unique, ordonnance optionnelle, instantane patient/medecin/montant/date, contenu medical, statut, remboursement, audit et version.
- `audit_events`: journal append-only des actions sans contenu medical sensible.

## Integrite

- une seule feuille par consultation via la contrainte unique existante;
- cles etrangeres internes entre consultation, prescription, medicaments et feuille;
- `@Version`/colonne `version` pour les conflits concurrents;
- index sur patient, medecin, consultation, prescription, statut, dates et creation;
- aucune suppression physique exposee pour les documents medicaux.

La migration additive `V5__persist_medical_records_crud.sql` retroalimente les nouvelles colonnes des feuilles depuis leur consultation avant de poser les contraintes `NOT NULL`.
