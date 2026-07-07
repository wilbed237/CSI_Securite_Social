import { ClipboardPlus, CreditCard, FileText, Home, Pill, Settings, Stethoscope, UserCircle2, UserPlus, Users } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import type { RoleName } from '../types/api';

export interface NavigationItem {
  labelKey: string;
  to: string;
  icon: LucideIcon;
  roles?: RoleName[];
}

export const navigationItems: NavigationItem[] = [
  { labelKey: 'nav.dashboard', to: '/app', icon: Home },
  { labelKey: 'nav.patients', to: '/app/insured', icon: Users, roles: ['AGENT', 'DOCTOR', 'GENERALIST', 'SPECIALIST'] },
  { labelKey: 'nav.newPatient', to: '/app/insured/new', icon: UserPlus, roles: ['AGENT'] },
  { labelKey: 'nav.doctors', to: '/app/doctors', icon: Stethoscope, roles: ['AGENT', 'DOCTOR', 'GENERALIST', 'SPECIALIST'] },
  { labelKey: 'nav.consultation', to: '/app/consultations', icon: ClipboardPlus, roles: ['AGENT', 'AGENT_SOCIAL', 'SOCIAL_AGENT', 'SECURITY_AGENT', 'DOCTOR', 'GENERALIST', 'SPECIALIST'] },
  { labelKey: 'nav.prescriptions', to: '/app/prescriptions', icon: Pill, roles: ['AGENT', 'AGENT_SOCIAL', 'SOCIAL_AGENT', 'SECURITY_AGENT', 'DOCTOR', 'GENERALIST', 'SPECIALIST'] },
  { labelKey: 'nav.sheets', to: '/app/disease-sheets', icon: FileText, roles: ['AGENT', 'AGENT_SOCIAL', 'SOCIAL_AGENT', 'SECURITY_AGENT', 'DOCTOR', 'GENERALIST', 'SPECIALIST'] },
  { labelKey: 'nav.reimbursements', to: '/app/reimbursements', icon: CreditCard, roles: ['AGENT'] },
  { labelKey: 'nav.account', to: '/app/account', icon: UserCircle2, roles: ['AGENT', 'ADMIN', 'SOCIAL_AGENT', 'AGENT_SOCIAL', 'SECURITY_AGENT', 'DOCTOR', 'GENERALIST', 'SPECIALIST'] },
  { labelKey: 'nav.settings', to: '/app/settings', icon: Settings, roles: ['AGENT', 'ADMIN', 'SOCIAL_AGENT', 'AGENT_SOCIAL', 'SECURITY_AGENT'] },
];
