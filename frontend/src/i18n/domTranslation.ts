import i18n from './index';

const frToEn: Record<string, string> = {
  'Tableau de bord médical': 'Medical dashboard', 'Patients couverts': 'Covered patients', 'Nouveau patient couvert': 'Add covered patient',
  'Médecins': 'Doctors', 'Consultation': 'Consultation', 'Ordonnances': 'Prescriptions', 'Feuilles de soins': 'Care sheets',
  'Prises en charge': 'Reimbursements', 'Paramètres': 'Settings', 'Déconnexion': 'Sign out', 'Console clinique': 'Clinical console',
  'Centre de coordination médicale': 'Medical coordination center', 'Fermer le menu': 'Close menu', 'Ouvrir le menu': 'Open menu',
  'Se connecter': 'Sign in', 'Créer un compte': 'Create account', 'Retour connexion': 'Back to sign in', 'Retour accueil': 'Back to home',
  'Accueil': 'Home', 'Connexion': 'Sign in', 'Inscription': 'Registration', 'Créer un utilisateur': 'Create user',
  'Accédez à votre espace agent ou médecin.': 'Access your agent or doctor workspace.', 'Mot de passe': 'Password',
  'Pas encore de compte ?': "Don't have an account yet?", 'Endpoint backend prévu pour ajouter des comptes applicatifs.': 'Create an application account and its linked business profile.',
  'Nom utilisateur': 'Username', 'Profil': 'Profile', 'Agent coordination médicale': 'Medical coordination agent',
  'Créer le profil de soins': 'Create care profile', 'Connexion réussie': 'Signed in successfully',
  'Utilisateur créé. Vous pouvez vous connecter.': 'User created. You can now sign in.', 'Email, téléphone ou utilisateur requis': 'Email, phone or username is required',
  'Mot de passe requis': 'Password is required', 'Nom utilisateur trop court': 'Username is too short', 'Prénom requis': 'First name is required',
  'Nom requis': 'Last name is required', 'Email invalide': 'Invalid email', '8 caractères minimum': 'Minimum 8 characters',
  'Spécialité obligatoire pour un spécialiste': 'Specialty is required for a specialist',
  'Un cockpit clinique pour vos parcours de soins.': 'A clinical command center for every care journey.',
  'Pilotez les dossiers patients, les praticiens, les ordonnances, les feuilles de soins et les prises en charge depuis une expérience médicale claire, sécurisée et responsive.': 'Manage patient records, practitioners, prescriptions, care sheets and reimbursements through a clear, secure and responsive medical experience.',
  'Patients suivis': 'Monitored patients', 'Validation clinique avant tout acte de soin': 'Clinical validation before every care procedure',
  'Actes cliniques': 'Clinical procedures', 'Consultations, ordonnances et feuilles de soins': 'Consultations, prescriptions and care sheets',
  'Prise en charge': 'Reimbursement', 'Calcul automatisé selon le parcours thérapeutique': 'Automated calculation based on the care pathway',
  'Orchestration clinique des patients, praticiens et parcours de soins.': 'Clinical orchestration for patients, practitioners and care pathways.',
  'Une console médicale fluide pour suivre les dossiers patients, les actes cliniques et la continuité thérapeutique.': 'A streamlined medical console for patient records, clinical procedures and continuity of care.',
  'Retour dashboard': 'Back to dashboard', 'Tableau de bord': 'Dashboard', 'Réessayer': 'Try again', 'Actualiser': 'Refresh',
  'Enregistrer': 'Save', 'Modifier': 'Edit', 'Configurer': 'Configure', 'Annuler': 'Cancel', 'Confirmer': 'Confirm', 'Rechercher': 'Search',
  'Réinitialiser': 'Reset', 'Nouveau': 'New', 'Nouveau médecin': 'New doctor', 'Associer': 'Assign', 'Tous': 'All', 'Toutes': 'All',
  'Actif': 'Active', 'Actifs': 'Active', 'Inactif': 'Inactive', 'Inactifs': 'Inactive', 'En service': 'On duty', 'Hors service': 'Off duty',
  'Généraliste': 'General practitioner', 'Généralistes': 'General practitioners', 'Spécialiste': 'Specialist', 'Spécialistes': 'Specialists',
  'Médecin généraliste': 'General practitioner', 'Médecin spécialiste': 'Specialist', 'Type de médecin': 'Doctor type', 'Type médecin': 'Doctor type',
  'Spécialité': 'Specialty', 'Spécialité requise': 'Required specialty', 'Service': 'Status', 'Statut': 'Status', 'Type': 'Type',
  'Période': 'Period', "Aujourd'hui": 'Today', 'Cette semaine': 'This week', 'Ce mois': 'This month',
  'Trois derniers mois': 'Last three months', 'Cette année': 'This year', 'Période personnalisée': 'Custom period',
  'Date de début': 'Start date', 'Date de fin': 'End date', 'Type de remboursement': 'Reimbursement type',
  'Filtres du tableau de bord': 'Dashboard filters', 'Patients assurés': 'Insured patients', 'Médecins en service': 'Doctors on duty',
  'Remboursements': 'Reimbursements', 'Montant remboursé': 'Reimbursed amount', 'Évolution mensuelle des patients': 'Monthly patient trend',
  'Remboursements par mois': 'Monthly reimbursements', 'Remboursements par statut': 'Reimbursements by status',
  'Médecins par type': 'Doctors by type', 'Consultations généralistes / spécialistes': 'General practitioner / specialist consultations',
  'Remboursements par prestation': 'Reimbursements by service', 'Montant remboursé par mois': 'Monthly reimbursed amount',
  'Médecins récemment ajoutés': 'Recently added doctors', 'Aucune donnée disponible.': 'No data available.',
  'Aucune activité récente disponible.': 'No recent activity available.', 'Aucune donnée de tableau de bord disponible.': 'No dashboard data available.',
  'Liste filtrable des patients assurés.': 'Filterable list of insured patients.', 'Annuaire des médecins généralistes et spécialistes enregistrés.': 'Directory of registered general practitioners and specialists.',
  'Identifiant': 'Identifier', 'Patient': 'Patient', 'Contact': 'Contact', 'Médecin traitant': 'Primary doctor', 'Matricule': 'Registration number',
  'Nom': 'Last name', 'Prénom': 'First name', 'Téléphone': 'Phone', 'Adresse': 'Address', 'Date naissance': 'Date of birth',
  'Email': 'Email', 'Aucun patient trouvé.': 'No patient found.', 'Aucun médecin trouvé.': 'No doctor found.',
  'Inscrire un patient couvert': 'Register a covered patient', 'Fonction réservée aux agents de coordination médicale.': 'Available to medical coordination agents only.',
  'Identifiant patient': 'Patient identifier', 'Enregistrer le dossier patient': 'Save patient record', 'Enregistrer un médecin': 'Register a doctor',
  "Respecte la règle d'exclusivité généraliste/spécialiste du référentiel clinique.": 'Applies the general practitioner/specialist exclusivity rule.',
  'Obligatoire uniquement pour un spécialiste': 'Required for specialists only', 'Enregistrer le médecin': 'Save doctor',
  'Créer une consultation': 'Create consultation', 'Numéro patient couvert': 'Covered patient number', 'Matricule médecin': 'Doctor registration number',
  'Coût consultation': 'Consultation cost', 'Début': 'Start', 'Fin': 'End', 'Créer la consultation': 'Create consultation',
  'Consultation créée': 'Consultation created', "Copiez l'identifiant pour les ordonnances et feuilles de soins.": 'Use this identifier for prescriptions and care sheets.',
  'Aucune consultation créée dans cette session.': 'No consultation created during this session.', 'Thérapeutiques': 'Medication',
  'Disponible pour tout médecin authentifié.': 'Available to every authenticated doctor.', 'ID consultation': 'Consultation ID',
  'Médicament': 'Medication', 'Posologie': 'Dosage', 'Enregistrer une ordonnance': 'Save prescription',
  'Consultation spécialiste': 'Specialist consultation', 'Action réservée au médecin généraliste.': 'Available to general practitioners only.',
  'Orienter un patient vers une spécialité médicale.': 'Refer a patient to a medical specialty.', 'Facteurs / justification': 'Factors / justification',
  'Orienter vers spécialiste': 'Refer to specialist', 'Dernière prescription': 'Latest prescription', 'Numéro': 'Number',
  'Créer une feuille': 'Create care sheet', 'Nécessite une consultation existante.': 'Requires an existing consultation.',
  'Diagnostic': 'Diagnosis', 'Créer une feuille de soins': 'Create care sheet', 'Rechercher une feuille': 'Find care sheet',
  'Numéro feuille': 'Care sheet number', 'Patient couvert': 'Covered patient', 'Médecin': 'Doctor', 'Coût': 'Cost',
  'Effectuer une prise en charge': 'Process a reimbursement', 'Numéro feuille de soins': 'Care sheet number', 'Mode paiement': 'Payment method',
  'Espèces': 'Cash', 'Virement bancaire': 'Bank transfer', 'Calculer et valider': 'Calculate and approve', 'Consulter une référence': 'Find a reference',
  'Référence prise en charge': 'Reimbursement reference', 'Référence': 'Reference', 'Feuille': 'Care sheet', 'Base': 'Base amount',
  'Taux': 'Rate', 'Montant': 'Amount', 'Confirmer la prise en charge': 'Confirm reimbursement', 'En attente': 'Pending',
  'Exécuté': 'Completed', 'Rejeté': 'Rejected', 'Médicaments': 'Medication', 'Hospitalisation': 'Hospitalization',
  'Examens médicaux': 'Medical exams', 'Imagerie': 'Imaging', 'Chirurgie': 'Surgery', 'Soins spécialisés': 'Specialized care', 'Autre': 'Other',
  'Agent': 'Agent', 'Tous les agents': 'All agents', 'Mes dossiers': 'My cases', 'Montant total': 'Total amount',
  'Nombre de dossiers': 'Number of cases', 'Montant moyen': 'Average amount', 'Aucun remboursement trouvé.': 'No reimbursement found.',
  'Paramètres généraux': 'General settings', 'Paramètres médicaux': 'Medical settings', 'Paramètres assurance santé': 'Health insurance settings',
  'Utilisateurs et sécurité': 'Users and security', 'Interface': 'Interface', 'Application et compte': 'Application and account',
  'Pays de résidence': 'Country of residence', 'Pays': 'Country', 'Langue': 'Language', 'Devise et activité financière': 'Currency and financial activity',
  'Utilisateur': 'User', 'Montant traité': 'Processed amount', 'Dossiers exécutés': 'Completed cases', 'Voir mes remboursements': 'View my reimbursements',
  'Valeur': 'Value', 'Thème actif': 'Active theme', 'Densité': 'Density', 'Animations': 'Animations', 'Confortable': 'Comfortable',
  'Compacte': 'Compact', 'Normales': 'Normal', 'Réduites': 'Reduced', 'Aucun référentiel médical configuré.': 'No medical reference configured.',
  'Clair clinique': 'Clinical light', 'Clair': 'Light', 'Nuit bleutée': 'Blue night', 'Bleu': 'Blue', 'Nuit ambrée': 'Amber night',
  'Orange': 'Orange', 'Nuit violacée': 'Purple night', 'Violet': 'Purple', 'Noir profond': 'Deep black', 'Noir': 'Black',
  'Bloc opératoire': 'Operating room', 'Vert': 'Green', 'Néon médical': 'Medical neon', 'Cyan': 'Cyan', 'Cardio nuit': 'Cardio night', 'Rose': 'Rose',
  "Palette d'interface": 'Interface palette', 'Thèmes Care Health': 'Care Health themes',
  'APPLICATION_NAME': 'Application name', 'COUNTRY': 'Country of residence', 'CURRENCY': 'Currency', 'LANGUAGE': 'Language',
  'TIMEZONE': 'Time zone', 'DATE_FORMAT': 'Date format', 'TIME_FORMAT': 'Time format', 'SPECIALTIES': 'Medical specialties',
  'CONSULTATION_TYPES': 'Consultation types', 'URGENCY_LEVELS': 'Urgency levels', 'INSURED_TYPES': 'Insured person types',
  'COVERAGE_RATES': 'Coverage rates', 'REIMBURSEMENT_STATUSES': 'Reimbursement statuses', 'REIMBURSEMENT_TYPES': 'Reimbursement types',
  'SESSION_DURATION_MINUTES': 'Session duration', 'PASSWORD_POLICY': 'Password policy',
  'Aucune règle assurance configurée.': 'No insurance rule configured.', 'Accès refusé': 'Access denied',
  'Paramètre enregistré': 'Setting saved', 'Pays de résidence enregistré': 'Country of residence saved', 'Langue mise à jour': 'Language updated',
  "Votre rôle ne permet pas d'accéder à cette fonctionnalité.": 'Your role does not allow access to this feature.', 'Page introuvable': 'Page not found',
  "La route demandée n'existe pas dans l'espace clinique Care Health.": 'The requested route does not exist in Care Health.',
  'Impossible d’afficher cette page': 'Unable to display this page', 'Une erreur inattendue est survenue.': 'An unexpected error occurred.',
  'Incident de parcours clinique': 'Clinical workflow incident', 'Aucune donnée clinique': 'No clinical data',
  'Aucun élément ne correspond aux critères actuels.': 'No item matches the current criteria.', 'Session expirée. Veuillez vous reconnecter.': 'Session expired. Please sign in again.',
  'Accès refusé pour votre rôle.': 'Access denied for your role.', 'Ressource introuvable.': 'Resource not found.',
  'Erreur serveur. Vérifiez que le backend est lancé.': 'Server error. Check that the backend is running.',
};

const enToFr = Object.fromEntries(Object.entries(frToEn).map(([fr, en]) => [en, fr]));
const attributes = ['aria-label', 'title', 'placeholder'];
let scheduled = false;
const observerOptions: MutationObserverInit = {
  childList: true,
  subtree: true,
  characterData: true,
  attributes: true,
  attributeFilter: attributes,
};
const observer = new MutationObserver(scheduleTranslation);

function dynamic(value: string, language: string) {
  if (language === 'en') return value
    .replace(/^Bonjour (.+)$/, 'Hello $1')
    .replace(/^Dernière actualisation : (.+)$/, 'Last updated: $1')
    .replace(/^(\d+) actifs, (\d+) nouveaux sur la période$/, '$1 active, $2 new during the period')
    .replace(/^(\d+) généralistes, (\d+) spécialistes$/, '$1 general practitioners, $2 specialists')
    .replace(/^(\d+) en attente, (\d+) rejetés$/, '$1 pending, $2 rejected')
    .replace(/^(\d+) dossiers traités par vous, moyenne (.+)$/, '$1 cases processed by you, average $2')
    .replace(/^Thème actuel : (.+)$/, 'Current theme: $1')
    .replace(/^Passer au thème suivant\. Thème actuel : (.+)$/, 'Switch to the next theme. Current theme: $1')
    .replace(/^Choisir un thème Care Health\. Thème actuel : (.+)$/, 'Choose a Care Health theme. Current theme: $1')
    .replace(/^(.+) \| Care Health$/, (_, title) => `${frToEn[title] ?? title} | Care Health`);
  return value
    .replace(/^Hello (.+)$/, 'Bonjour $1')
    .replace(/^Last updated: (.+)$/, 'Dernière actualisation : $1')
    .replace(/^(\d+) active, (\d+) new during the period$/, '$1 actifs, $2 nouveaux sur la période')
    .replace(/^(\d+) general practitioners, (\d+) specialists$/, '$1 généralistes, $2 spécialistes')
    .replace(/^(\d+) pending, (\d+) rejected$/, '$1 en attente, $2 rejetés')
    .replace(/^(\d+) cases processed by you, average (.+)$/, '$1 dossiers traités par vous, moyenne $2')
    .replace(/^Current theme: (.+)$/, 'Thème actuel : $1')
    .replace(/^Switch to the next theme\. Current theme: (.+)$/, 'Passer au thème suivant. Thème actuel : $1')
    .replace(/^Choose a Care Health theme\. Current theme: (.+)$/, 'Choisir un thème Care Health. Thème actuel : $1')
    .replace(/^(.+) \| Care Health$/, (_, title) => `${enToFr[title] ?? title} | Care Health`);
}

function translateValue(value: string, language: string) {
  const leading = value.match(/^\s*/)?.[0] ?? '';
  const trailing = value.match(/\s*$/)?.[0] ?? '';
  const core = value.trim();
  const dictionary = language === 'en' ? frToEn : enToFr;
  return `${leading}${dictionary[core] ?? dynamic(core, language)}${trailing}`;
}

function translateTree() {
  observer.disconnect();
  try {
    const language = i18n.resolvedLanguage === 'en' ? 'en' : 'fr';
    const walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);
    let node: Node | null;
    while ((node = walker.nextNode())) {
      if (node.parentElement?.closest('script,style')) continue;
      const next = translateValue(node.nodeValue ?? '', language);
      if (next !== node.nodeValue) node.nodeValue = next;
    }
    document.querySelectorAll<HTMLElement>('*').forEach((element) => attributes.forEach((attribute) => {
      const value = element.getAttribute(attribute);
      if (!value) return;
      const next = translateValue(value, language);
      if (next !== value) element.setAttribute(attribute, next);
    }));
    document.title = translateValue(document.title, language);
  } finally {
    observer.observe(document.documentElement, observerOptions);
  }
}

function scheduleTranslation() {
  if (scheduled) return;
  scheduled = true;
  requestAnimationFrame(() => { scheduled = false; translateTree(); });
}

i18n.on('languageChanged', scheduleTranslation);
observer.observe(document.documentElement, observerOptions);
if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', scheduleTranslation); else scheduleTranslation();
