import React from "react";
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TextField } from "@mui/material";
import type { UserRequestDto } from "../../../api/types";

type Props = {
  open: boolean;
  title: string;
  initial?: Partial<UserRequestDto>;
  onClose: () => void;
  onSubmit: (dto: UserRequestDto) => void;
  submitLabel?: string;
  busy?: boolean;
};

export function UserFormDialog({ open, title, initial, onClose, onSubmit, submitLabel, busy }: Props) {
  const [fullName, setFullName] = React.useState(initial?.fullName ?? "");
  const [email, setEmail] = React.useState(initial?.email ?? "");

  React.useEffect(() => {
    setFullName(initial?.fullName ?? "");
    setEmail(initial?.email ?? "");
  }, [open, initial]);

  const canSubmit = fullName.trim().length > 0 && email.trim().length > 0;

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Full name" value={fullName} onChange={(e) => setFullName(e.target.value)} />
          <TextField label="Email" value={email} onChange={(e) => setEmail(e.target.value)} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button
          variant="contained"
          disabled={!canSubmit || !!busy}
          onClick={() => onSubmit({ fullName: fullName.trim(), email: email.trim() })}
        >
          {submitLabel ?? "Save"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

