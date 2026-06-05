import { ClipboardPlus, CreditCard, FileText, Home, Pill, Stethoscope, UserPlus, Users } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import type { RoleName } from '../types/api';

export interface NavigationItem {
  label: string;
  to: string;
  icon: LucideIcon;
  roles?: RoleName[];
}

export const navigationItems: NavigationItem[] = [
  { label: 'Dashboard', to: '/app', icon: Home },
  { label: 'Patients couverts', to: '/app/insured', icon: Users, roles: ['AGENT', 'DOCTOR', 'GENERALIST', 'SPECIALIST'] },
  { label: 'Nouveau patient couvert', to: '/app/insured/new', icon: UserPlus, roles: ['AGENT'] },
  { label: 'Médecins', to: '/app/doctors', icon: Stethoscope, roles: ['AGENT', 'DOCTOR', 'GENERALIST', 'SPECIALIST'] },
  { label: 'Consultation', to: '/app/consultations/new', icon: ClipboardPlus, roles: ['DOCTOR', 'GENERALIST', 'SPECIALIST'] },
  { label: 'Ordonnances', to: '/app/ordonnances', icon: Pill, roles: ['DOCTOR', 'GENERALIST', 'SPECIALIST'] },
  { label: 'Feuilles de soins', to: '/app/disease-sheets', icon: FileText, roles: ['AGENT', 'DOCTOR', 'GENERALIST', 'SPECIALIST'] },
  { label: 'Prises en charge', to: '/app/reimbursements', icon: CreditCard, roles: ['AGENT'] },
];
