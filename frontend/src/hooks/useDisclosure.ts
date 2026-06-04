import { useState } from 'react';

export function useDisclosure(initial = false) {
  const [open, setOpen] = useState(initial);
  return { open, openModal: () => setOpen(true), closeModal: () => setOpen(false), setOpen };
}
