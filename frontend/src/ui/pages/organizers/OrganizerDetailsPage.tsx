import { useParams, Link as RouterLink } from "react-router-dom";
import { Alert, Button, Divider, Paper, Stack, Typography } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { getOrganizerById } from "../../../api/organizers";
import { listEvents } from "../../../api/events";
import type { EventResponseDto } from "../../../api/types";
import { ErrorAlert } from "../../components/ErrorAlert";
import { eventStatusLabel, formatDateTimeRu, formatPriceBr } from "../../format";

export function OrganizerDetailsPage() {
  const { id } = useParams();
  const organizerId = Number(id);

  const organizerQuery = useQuery({
    queryKey: ["organizers", "byId", organizerId],
    queryFn: () => getOrganizerById(organizerId),
    enabled: Number.isFinite(organizerId) && organizerId > 0,
  });

  const eventsQuery = useQuery({
    queryKey: ["events", "byOrganizerName", organizerId],
    queryFn: async () => {
      const all = await listEvents();
      const organizerName = organizerQuery.data?.name ?? "";
      return organizerName ? all.filter((e) => e.organizerName === organizerName) : [];
    },
    enabled: !!organizerQuery.data?.name,
  });

  const events: EventResponseDto[] = eventsQuery.data ?? [];
  const title = organizerQuery.data?.name ?? "Организатор";

  return (
    <Stack spacing={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="center">
        <Typography variant="h4">{title}</Typography>
        <Button component={RouterLink} to="/organizers" variant="outlined">
          Назад
        </Button>
      </Stack>

      <ErrorAlert error={organizerQuery.error || eventsQuery.error} />
      {!organizerQuery.data && organizerQuery.isLoading && <Alert severity="info">Загрузка…</Alert>}

      {organizerQuery.data && (
        <Paper sx={{ p: 2 }}>
          <Stack spacing={1}>
            <Typography color="text.secondary">
              Контакты: {organizerQuery.data.contactInfo ?? "—"}
            </Typography>
          </Stack>
        </Paper>
      )}

      <Paper sx={{ p: 2 }}>
        <Stack spacing={1}>
          <Typography variant="h6">Мероприятия организатора</Typography>
          <Divider />
          {events.map((e) => (
            <Paper key={e.id} variant="outlined" sx={{ p: 1.5 }}>
              <Typography fontWeight={600}>{e.name}</Typography>
              <Typography variant="body2" color="text.secondary">
                {eventStatusLabel(e)} · {formatPriceBr(e.ticketPrice)}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Начало: {formatDateTimeRu(e.startDate)}
              </Typography>
              <Button component={RouterLink} to={`/events/${e.id}`} size="small" sx={{ mt: 1 }}>
                Открыть
              </Button>
            </Paper>
          ))}
          {events.length === 0 && <Typography color="text.secondary">Пока нет опубликованных событий.</Typography>}
        </Stack>
      </Paper>
    </Stack>
  );
}
