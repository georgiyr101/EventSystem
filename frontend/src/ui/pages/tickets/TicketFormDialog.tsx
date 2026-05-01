import React from "react";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
} from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { listEvents } from "../../../api/events";
import { listUsers } from "../../../api/users";
import type { TicketRequestDto } from "../../../api/types";

type Props = {
  open: boolean;
  title: string;
  initial?: Partial<TicketRequestDto>;
  onClose: () => void;
  onSubmit: (dto: TicketRequestDto) => void;
  submitLabel?: string;
  busy?: boolean;
};

export function TicketFormDialog({ open, title, initial, onClose, onSubmit, submitLabel, busy }: Props) {
  const [eventId, setEventId] = React.useState<number>(initial?.eventId ?? 0);
  const [userId, setUserId] = React.useState<number>(initial?.userId ?? 0);
  const [barcode, setBarcode] = React.useState(initial?.barcode ?? "");

  const eventsQuery = useQuery({
    queryKey: ["events"],
    queryFn: listEvents,
    enabled: open,
  });
  const usersQuery = useQuery({
    queryKey: ["users"],
    queryFn: listUsers,
    enabled: open,
  });

  React.useEffect(() => {
    setEventId(initial?.eventId ?? 0);
    setUserId(initial?.userId ?? 0);
    setBarcode(initial?.barcode ?? "");
  }, [open, initial]);

  const events = eventsQuery.data ?? [];
  const users = usersQuery.data ?? [];

  React.useEffect(() => {
    if (!open || events.length === 0) return;
    if (!eventId || !events.some((e) => e.id === eventId)) {
      setEventId(events[0].id);
    }
  }, [open, events, eventId]);

  React.useEffect(() => {
    if (!open || users.length === 0) return;
    if (!userId || !users.some((u) => u.id === userId)) {
      setUserId(users[0].id);
    }
  }, [open, users, userId]);

  const canSubmit = eventId > 0 && userId > 0 && barcode.trim().length > 0;

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <FormControl fullWidth>
            <InputLabel id="ticket-event-label">Мероприятие</InputLabel>
            <Select
              labelId="ticket-event-label"
              label="Мероприятие"
              value={eventId || ""}
              onChange={(e) => setEventId(Number(e.target.value))}
            >
              {events.map((ev) => (
                <MenuItem key={ev.id} value={ev.id}>
                  {ev.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <FormControl fullWidth>
            <InputLabel id="ticket-user-label">Покупатель</InputLabel>
            <Select
              labelId="ticket-user-label"
              label="Покупатель"
              value={userId || ""}
              onChange={(e) => setUserId(Number(e.target.value))}
            >
              {users.map((u) => (
                <MenuItem key={u.id} value={u.id}>
                  {[u.fullName?.trim(), u.email].filter(Boolean).join(" · ") || u.email}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            label="Номер билета (штрихкод)"
            value={barcode}
            onChange={(e) => setBarcode(e.target.value)}
            fullWidth
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button
          variant="contained"
          disabled={!canSubmit || !!busy}
          onClick={() =>
            onSubmit({
              eventId,
              userId,
              barcode: barcode.trim(),
            })
          }
        >
          {submitLabel ?? "Сохранить"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
