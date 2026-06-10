UPDATE doctors
SET email = 'generaliste@csi.local'
WHERE id = '00000000-0000-0000-0000-000000000101'
  AND auth_user_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM doctors existing
      WHERE LOWER(existing.email) = LOWER('generaliste@csi.local')
        AND existing.id <> doctors.id
  );

UPDATE doctors
SET email = 'specialiste@csi.local'
WHERE id = '00000000-0000-0000-0000-000000000102'
  AND auth_user_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM doctors existing
      WHERE LOWER(existing.email) = LOWER('specialiste@csi.local')
        AND existing.id <> doctors.id
  );
