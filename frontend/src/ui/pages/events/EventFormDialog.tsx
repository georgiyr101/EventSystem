import React from "react";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  InputAdornment,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
} from "@mui/material";
import type { CategoryResponseDto, EventRequestDto, OrganizerResponseDto } from "../../../api/types";

type Props = {
  open: boolean;
  title: string;
  organizers: OrganizerResponseDto[];
  categories: CategoryResponseDto[];
  initial?: Partial<EventRequestDto> | null;
  onClose: () => void;
  onSubmit: (dto: EventRequestDto) => void;
  submitLabel?: string;
  busy?: boolean;
};

function toLocalDateTimeInputValue(v?: string) {
  if (!v) return "";
  // from ISO "2026-06-15T18:00:00" -> "2026-06-15T18:00"
  return v.length >= 16 ? v.slice(0, 16) : v;
}

export function EventFormDialog(props: Props) {
  const { open, title, onClose, onSubmit, organizers, categories, initial, submitLabel, busy } = props;

  const [name, setName] = React.useState(initial?.name ?? "");
  const [startDate, setStartDate] = React.useState(toLocalDateTimeInputValue(initial?.startDate));
  const [endDate, setEndDate] = React.useState(toLocalDateTimeInputValue(initial?.endDate));
  const [maxParticipants, setMaxParticipants] = React.useState(String(initial?.maxParticipants ?? 1));
  const [ticketPrice, setTicketPrice] = React.useState(String(initial?.ticketPrice ?? 0));
  const [organizerId, setOrganizerId] = React.useState<number>(initial?.organizerId ?? organizers[0]?.id ?? 0);
  const [categoryIds, setCategoryIds] = React.useState<number[]>(initial?.categoryIds ?? []);

  React.useEffect(() => {
    setName(initial?.name ?? "");
    setStartDate(toLocalDateTimeInputValue(initial?.startDate));
    setEndDate(toLocalDateTimeInputValue(initial?.endDate));
    setMaxParticipants(String(initial?.maxParticipants ?? 1));
    setTicketPrice(String(initial?.ticketPrice ?? 0));
    setOrganizerId(initial?.organizerId ?? organizers[0]?.id ?? 0);
    setCategoryIds(initial?.categoryIds ?? []);
  }, [open, initial, organizers]);

  const canSubmit = name.trim().length > 0 && organizerId > 0 && startDate && endDate;

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Название" value={name} onChange={(e) => setName(e.target.value)} fullWidth />
          <TextField
            type="datetime-local"
            label="Начало"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
            fullWidth
          />
          <TextField
            type="datetime-local"
            label="Окончание"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
            fullWidth
          />

          <Stack direction="row" spacing={2}>
            <TextField
              label="Мест для гостей"
              type="number"
              value={maxParticipants}
              onChange={(e) => setMaxParticipants(e.target.value)}
              fullWidth
            />
            <TextField
              label="Цена билета"
              type="number"
              value={ticketPrice}
              onChange={(e) => setTicketPrice(e.target.value)}
              fullWidth
              InputProps={{
                endAdornment: <InputAdornment position="end">Br</InputAdornment>,
              }}
            />
          </Stack>

          <FormControl fullWidth>
            <InputLabel id="organizer-label">Организатор</InputLabel>
            <Select
              labelId="organizer-label"
              value={organizerId}
              label="Организатор"
              onChange={(e) => setOrganizerId(Number(e.target.value))}
            >
              {organizers.map((o) => (
                <MenuItem key={o.id} value={o.id}>
                  {o.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          <FormControl fullWidth>
            <InputLabel id="categories-label">Категории</InputLabel>
            <Select
              labelId="categories-label"
              multiple
              value={categoryIds}
              label="Категории"
              onChange={(e) => setCategoryIds(e.target.value as number[])}
              renderValue={(selected) =>
                (selected as number[])
                  .map((cid) => categories.find((c) => c.id === cid)?.name)
                  .filter(Boolean)
                  .join(", ")
              }
            >
              {categories.map((c) => (
                <MenuItem key={c.id} value={c.id}>
                  {c.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button
          variant="contained"
          disabled={!canSubmit || !!busy}
          onClick={() =>
            onSubmit({
              name: name.trim(),
              startDate: startDate.length === 16 ? `${startDate}:00` : startDate,
              endDate: endDate.length === 16 ? `${endDate}:00` : endDate,
              maxParticipants: Number(maxParticipants),
              ticketPrice: Number(ticketPrice),
              organizerId,
              categoryIds,
            })
          }
        >
          {submitLabel ?? "Сохранить"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

