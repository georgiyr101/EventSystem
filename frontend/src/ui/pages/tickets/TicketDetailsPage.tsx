import { useParams, Link as RouterLink } from "react-router-dom";
import { Alert, Button, Paper, Stack, Typography } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { getTicketById } from "../../../api/tickets";
import { ErrorAlert } from "../../components/ErrorAlert";
import { formatDateTimeRu } from "../../format";

export function TicketDetailsPage() {
  const { id } = useParams();
  const ticketId = Number(id);

  const q = useQuery({
    queryKey: ["tickets", "byId", ticketId],
    queryFn: () => getTicketById(ticketId),
    enabled: Number.isFinite(ticketId) && ticketId > 0,
  });

  const title = q.data?.eventName ?? "Билет";

  return (
    <Stack spacing={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="center">
        <Typography variant="h4">{title}</Typography>
        <Button component={RouterLink} to="/tickets" variant="outlined">
          Назад
        </Button>
      </Stack>

      <ErrorAlert error={q.error} />
      {!q.data && q.isLoading && <Alert severity="info">Загрузка…</Alert>}

      {q.data && (
        <Paper sx={{ p: 2 }}>
          <Stack spacing={1}>
            <Typography fontWeight={600}>{q.data.eventName}</Typography>
            <Typography color="text.secondary">Гость: {q.data.userEmail}</Typography>
            <Typography>Номер билета: {q.data.barcode}</Typography>
            <Typography>Куплен: {formatDateTimeRu(q.data.purchaseDate)}</Typography>
            <Button component={RouterLink} to={`/events/${q.data.eventId}`} variant="contained" sx={{ mt: 1, alignSelf: "flex-start" }}>
              Страница мероприятия
            </Button>
          </Stack>
        </Paper>
      )}
    </Stack>
  );
}
