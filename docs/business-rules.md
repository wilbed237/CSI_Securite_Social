# Care Health - Regles metier

## Acteurs et acces

- L'assure est un dossier metier, pas un utilisateur authentifie.
- `AGENT` porte les operations administratives et financieres. Les anciens alias agent restent acceptes a l'inscription, mais sont normalises vers `AGENT`.
- `GENERALIST` et `SPECIALIST` impliquent `DOCTOR`.
- Un medecin ne peut agir que sous son propre matricule, derive de son `authUserId`.
- Un generaliste accede aux assures dont il est le medecin traitant. Un specialiste accede aux assures qui lui sont adresses.
- `ADMIN` n'accorde pas implicitement l'acces aux observations et diagnostics medicaux.

## Assures et medecin traitant

- Le numero d'assurance est unique.
- Un assure inactif ne peut pas faire l'objet d'une nouvelle consultation.
- Le medecin traitant doit etre un generaliste actif.
- Toute affectation ferme l'affectation courante et cree une nouvelle ligne d'historique.

## Consultations et prescriptions

- Le cout d'une consultation est strictement positif et sa fin est posterieure a son debut.
- Le medecin authentifie doit correspondre au medecin de la consultation.
- Une prescription appartient a une consultation existante realisee par son auteur.
- Une ligne medicament contient nom, posologie, frequence, duree, quantite et instructions optionnelles.
- Seul un generaliste peut creer une orientation.
- Une orientation cible au moins un specialiste actif de la specialite demandee.

## Feuilles de maladie

Cycle autorise :

```text
DRAFT -> ISSUED -> SUBMITTED -> UNDER_REVIEW -> APPROVED -> PAID
                                  |             |
                                  +-> REJECTED  +-> CANCELLED
```

- Une consultation ne produit qu'une feuille.
- Le medecin cree et emet la feuille. L'agent la soumet au controle puis la complete.
- Une feuille payee ne peut plus etre modifiee.

## Remboursements

- Le calcul est realise cote backend : generaliste 100 %, specialiste 80 %.
- Le montant rembourse est arrondi a deux decimales.
- Une feuille ne possede qu'une demande de remboursement.
- Cycle autorise : `PENDING -> APPROVED -> EXECUTED` ou `PENDING -> REJECTED`.
- Le rejet exige un motif.
- Seule une demande approuvee peut etre executee.
- L'execution est idempotente et conserve agent, date, mode et reference de paiement.
- Les coordonnees bancaires sont chiffrees au repos et masquees dans les DTO et les logs.

## Audit

Les actions sensibles produisent un evenement append-only contenant `actorUserId`, `actorRole`, `action`, `resourceType`, `resourceId`, `timestamp`, `result` et des metadonnees non sensibles.
