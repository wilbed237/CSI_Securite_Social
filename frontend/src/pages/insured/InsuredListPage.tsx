import { Search, UserPlus } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { PageHeader } from '../../components/PageHeader';
import { RoleGate } from '../../components/RoleGate';
import { Button } from '../../components/ui/Button';
import { Card, CardHeader } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { usePageTitle } from '../../hooks/usePageTitle';

export function InsuredListPage() {
  usePageTitle('Patients couverts');
  const [insuranceNumber, setInsuranceNumber] = useState('ASS-0001');
  const navigate = useNavigate();
  const search = () => insuranceNumber.trim() && navigate(`/app/insured/${insuranceNumber.trim()}`);

  return (
    <div>
      <PageHeader title="Patients couverts" description="Le backend expose la recherche par identifiant patient et l'inscription par agent." action={<RoleGate roles={['AGENT']}><Link to="/app/insured/new"><Button icon={<UserPlus className="h-4 w-4" />}>Nouveau</Button></Link></RoleGate>} />
      <Card>
        <CardHeader title="Rechercher un patient couvert" description="Saisissez le identifiant patient généré lors de l'inscription." />
        <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
          <Input label="Identifiant patient" value={insuranceNumber} onChange={(event) => setInsuranceNumber(event.target.value)} />
          <Button className="sm:mb-0.5" icon={<Search className="h-4 w-4" />} onClick={search}>Rechercher</Button>
        </div>
      </Card>
    </div>
  );
}
