import React from "react";
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TextField } from "@mui/material";
import type { CategoryRequestDto } from "../../../api/types";

type Props = {
  open: boolean;
  title: string;
  initial?: Partial<CategoryRequestDto>;
  onClose: () => void;
  onSubmit: (dto: CategoryRequestDto) => void;
  submitLabel?: string;
  busy?: boolean;
};

export function CategoryFormDialog({ open, title, initial, onClose, onSubmit, submitLabel, busy }: Props) {
  const [name, setName] = React.useState(initial?.name ?? "");

  React.useEffect(() => {
    setName(initial?.name ?? "");
  }, [open, initial]);

  const canSubmit = name.trim().length > 0;

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Name" value={name} onChange={(e) => setName(e.target.value)} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button
          variant="contained"
          disabled={!canSubmit || !!busy}
          onClick={() => onSubmit({ name: name.trim() })}
        >
          {submitLabel ?? "Save"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

