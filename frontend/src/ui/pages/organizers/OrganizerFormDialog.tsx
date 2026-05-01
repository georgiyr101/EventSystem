import React from "react";
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TextField } from "@mui/material";
import type { OrganizerRequestDto } from "../../../api/types";

type Props = {
  open: boolean;
  title: string;
  initial?: Partial<OrganizerRequestDto>;
  onClose: () => void;
  onSubmit: (dto: OrganizerRequestDto) => void;
  submitLabel?: string;
  busy?: boolean;
};

export function OrganizerFormDialog({ open, title, initial, onClose, onSubmit, submitLabel, busy }: Props) {
  const [name, setName] = React.useState(initial?.name ?? "");
  const [contactInfo, setContactInfo] = React.useState(initial?.contactInfo ?? "");

  React.useEffect(() => {
    setName(initial?.name ?? "");
    setContactInfo(initial?.contactInfo ?? "");
  }, [open, initial]);

  const canSubmit = name.trim().length > 0 && contactInfo.trim().length > 0;

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Name" value={name} onChange={(e) => setName(e.target.value)} />
          <TextField label="Contact info" value={contactInfo} onChange={(e) => setContactInfo(e.target.value)} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button
          variant="contained"
          disabled={!canSubmit || !!busy}
          onClick={() => onSubmit({ name: name.trim(), contactInfo: contactInfo.trim() })}
        >
          {submitLabel ?? "Save"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

