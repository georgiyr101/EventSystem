import React from "react";
import { useParams, Link as RouterLink } from "react-router-dom";
import {
  Alert,
  Box,
  Button,
  Chip,
  Divider,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Stack,
  Typography,
} from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { changeEventStatus, getEventById } from "../../../api/events";
import { getOrganizerById } from "../../../api/organizers";
import { createTicket, listTickets } from "../../../api/tickets";
import type { TicketResponseDto } from "../../../api/types";
import { useAuth } from "../../../auth/AuthContext";
import { ErrorAlert } from "../../components/ErrorAlert";
import { eventStatusLabel, formatDateTimeRu, formatPriceBr } from "../../format";

function randomBarcode(): string {
  const chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  let s = "";
  for (let i = 0; i < 10; i++) s += chars[Math.floor(Math.random() * chars.length)];
  return s;
}

const STATUS_CHANGES: { value: string; label: string }[] = [
  { value: "PLANNED", label: "Запланировано" },
  { value: "ONGOING", label: "Идёт сейчас" },
  { value: "COMPLETED", label: "Завершено" },
  { value: "CANCELLED", label: "Отменено" },
  { value: "SOLD_OUT", label: "Билеты проданы" },
];

export function EventDetailsPage() {
  const { id } = useParams();
  const eventId = Number(id);
  const qc = useQueryClient();
  const { profile } = useAuth();

  const isAdmin = profile?.role === "ADMIN";
  const isUser = profile?.role === "USER";
  const isOrganizer = profile?.role === "ORGANIZER";
  const myOrganizerId = profile?.organizerId ?? null;

  const myOrganizerQuery = useQuery({
    queryKey: ["organizers", "mine", myOrganizerId],
    queryFn: () => getOrganizerById(myOrganizerId!),
    enabled: isOrganizer && myOrganizerId != null && myOrganizerId > 0,
  });

  const eventQuery = useQuery({
    queryKey: ["events", "byId", eventId],
    queryFn: () => getEventById(eventId),
    enabled: Number.isFinite(eventId) && eventId > 0,
  });

  const ticketsQuery = useQuery({
    queryKey: ["tickets", "allForEvent", eventId],
    queryFn: async () => {
      const all = await listTickets();
      return all.filter((t) => t.eventId === eventId);
    },
    enabled: isAdmin && Number.isFinite(eventId) && eventId > 0,
  });

  const [newStatus, setNewStatus] = React.useState("");
  const statusMut = useMutation({
    mutationFn: () => changeEventStatus(eventId, newStatus),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["events"] });
      await qc.invalidateQueries({ queryKey: ["events", "byId", eventId] });
      setNewStatus("");
    },
  });

  const buyMut = useMutation({
    mutationFn: () =>
      createTicket({
        eventId,
        barcode: randomBarcode(),
      }),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["tickets"] });
    },
  });

  const e = eventQuery.data;
  const tickets: TicketResponseDto[] = ticketsQuery.data ?? [];

  const isMyEventAsOrganizer =
    isOrganizer && e != null && myOrganizerQuery.data?.name === e.organizerName;
  const canChangeEventStatus = isAdmin || isMyEventAsOrganizer;

  const cannotBuy =
    e &&
    (e.statusCode === "SOLD_OUT" || e.statusCode === "COMPLETED" || e.statusCode === "CANCELLED");

  return (
    <Stack spacing={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="center">
        <Typography variant="h4">{e?.name ?? "Мероприятие"}</Typography>
        <Button component={RouterLink} to="/events" variant="outlined">
          К каталогу
        </Button>
      </Stack>

      <ErrorAlert error={eventQuery.error || ticketsQuery.error || statusMut.error || buyMut.error} />

      {!e && eventQuery.isLoading && <Alert severity="info">Загрузка…</Alert>}

      {e && (
        <Paper sx={{ p: 2 }}>
          <Stack spacing={1}>
            <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap">
              <Chip size="small" label={eventStatusLabel(e)} />
            </Stack>
            <Typography color="text.secondary">Организатор: {e.organizerName}</Typography>
            <Typography color="text.secondary">Начало: {formatDateTimeRu(e.startDate)}</Typography>
            <Typography color="text.secondary">Цена билета: {formatPriceBr(e.ticketPrice)}</Typography>
            <Typography color="text.secondary">
              Категории: {(e.categoryNames ?? []).join(", ") || "—"}
            </Typography>

            {isUser && (
              <>
                <Divider sx={{ my: 1 }} />
                <Box>
                  <Typography variant="subtitle1" sx={{ mb: 1 }}>
                    Купить билет
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                    Номер билета сформируется автоматически после оплаты.
                  </Typography>
                  <Button variant="contained" disabled={buyMut.isPending || !!cannotBuy} onClick={() => buyMut.mutate()}>
                    {buyMut.isPending ? "Покупка…" : "Купить билет"}
                  </Button>
                  {buyMut.isSuccess && (
                    <Alert severity="success" sx={{ mt: 1 }}>
                      Билет куплен. Смотрите раздел «Мои билеты» в меню профиля.
                    </Alert>
                  )}
                </Box>
              </>
            )}

            {canChangeEventStatus && (
              <>
                <Divider sx={{ my: 1 }} />
                <Box>
                  <Typography variant="subtitle1" sx={{ mb: 1 }}>
                    Изменить статус
                  </Typography>
                  <Stack direction={{ xs: "column", sm: "row" }} spacing={1}>
                    <FormControl fullWidth size="small">
                      <InputLabel id="status-change-label">Новый статус</InputLabel>
                      <Select
                        labelId="status-change-label"
                        label="Новый статус"
                        value={newStatus}
                        onChange={(ev) => setNewStatus(ev.target.value)}
                      >
                        {STATUS_CHANGES.map((o) => (
                          <MenuItem key={o.value} value={o.value}>
                            {o.label}
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                    <Button
                      variant="contained"
                      disabled={!newStatus || statusMut.isPending}
                      onClick={() => statusMut.mutate()}
                      sx={{ flexShrink: 0 }}
                    >
                      Применить
                    </Button>
                  </Stack>
                </Box>
              </>
            )}
          </Stack>
        </Paper>
      )}

      {isAdmin && (
        <Paper sx={{ p: 2 }}>
          <Stack spacing={1}>
            <Typography variant="h6">Билеты на это мероприятие</Typography>
            <Divider />
            {tickets.map((t) => (
              <Paper key={t.id} variant="outlined" sx={{ p: 1.5 }}>
                <Typography fontWeight={600}>{t.userEmail}</Typography>
                <Typography variant="body2" color="text.secondary">
                  Номер билета: {t.barcode}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Куплен: {formatDateTimeRu(t.purchaseDate)}
                </Typography>
              </Paper>
            ))}
            {tickets.length === 0 && <Typography color="text.secondary">Пока никто не купил билеты.</Typography>}
          </Stack>
        </Paper>
      )}
    </Stack>
  );
}
