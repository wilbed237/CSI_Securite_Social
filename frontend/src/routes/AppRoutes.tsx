import { createBrowserRouter, Navigate } from 'react-router-dom';
import { AuthLayout } from '../layouts/AuthLayout';
import { AppLayout } from '../layouts/AppLayout';
import { ProtectedRoute } from './ProtectedRoute';
import { DashboardPage } from '../pages/DashboardPage';
import { LandingPage } from '../pages/LandingPage';
import { LoginPage } from '../pages/auth/LoginPage';
import { RegisterPage } from '../pages/auth/RegisterPage';
import { InsuredListPage } from '../pages/insured/InsuredListPage';
import { InsuredCreatePage } from '../pages/insured/InsuredCreatePage';
import { InsuredDetailPage } from '../pages/insured/InsuredDetailPage';
import { DoctorListPage } from '../pages/doctors/DoctorListPage';
import { DoctorCreatePage } from '../pages/doctors/DoctorCreatePage';
import { ConsultationCreatePage } from '../pages/medical/ConsultationCreatePage';
import { PrescriptionPage } from '../pages/medical/PrescriptionPage';
import { DiseaseSheetPage } from '../pages/medical/DiseaseSheetPage';
import { ReimbursementPage } from '../pages/reimbursement/ReimbursementPage';
import { ForbiddenPage } from '../pages/errors/ForbiddenPage';
import { NotFoundPage } from '../pages/errors/NotFoundPage';

export const router = createBrowserRouter([
  { path: '/', element: <LandingPage /> },
  {
    element: <AuthLayout />,
    children: [
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },
    ],
  },
  {
    path: '/app',
    element: <ProtectedRoute><AppLayout /></ProtectedRoute>,
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'insured', element: <InsuredListPage /> },
      { path: 'insured/new', element: <ProtectedRoute roles={['AGENT']}><InsuredCreatePage /></ProtectedRoute> },
      { path: 'insured/:insuranceNumber', element: <InsuredDetailPage /> },
      { path: 'doctors', element: <DoctorListPage /> },
      { path: 'doctors/new', element: <ProtectedRoute roles={['AGENT']}><DoctorCreatePage /></ProtectedRoute> },
      { path: 'consultations/new', element: <ProtectedRoute roles={['DOCTOR', 'GENERALIST', 'SPECIALIST']}><ConsultationCreatePage /></ProtectedRoute> },
      { path: 'prescriptions', element: <ProtectedRoute roles={['DOCTOR', 'GENERALIST', 'SPECIALIST']}><PrescriptionPage /></ProtectedRoute> },
      { path: 'disease-sheets', element: <DiseaseSheetPage /> },
      { path: 'reimbursements', element: <ProtectedRoute roles={['AGENT']}><ReimbursementPage /></ProtectedRoute> },
      { path: '*', element: <Navigate to="/app" replace /> },
    ],
  },
  { path: '/forbidden', element: <ForbiddenPage /> },
  { path: '*', element: <NotFoundPage /> },
]);
