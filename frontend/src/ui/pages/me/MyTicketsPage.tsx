import { Link as RouterLink } from "react-router-dom";
import { Button, Card, CardActions, CardContent, Paper, Stack, Typography } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { listTickets } from "../../../api/tickets";
import { ErrorAlert } from "../../components/ErrorAlert";
import { formatDateTimeRu } from "../../format";

export function MyTicketsPage() {
  const ticketsQuery = useQuery({
    queryKey: ["tickets", "mine"],
    queryFn: () => listTickets(),
  });

  const rows = ticketsQuery.data ?? [];

  return (
    <Stack spacing={2}>
      <Typography variant="h4">Мои билеты</Typography>
      <Typography color="text.secondary">Все ваши билеты в одном месте.</Typography>
      <ErrorAlert error={ticketsQuery.error} />
      <Paper sx={{ p: 2, border: "1px solid rgba(15,23,42,0.06)" }}>
        <Stack spacing={1.5}>
          {rows.map((t) => (
            <Card key={t.id} variant="outlined" sx={{ borderColor: "rgba(15,23,42,0.08)" }}>
              <CardContent>
                <Typography variant="h6" sx={{ mb: 0.5 }}>
                  {t.eventName}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Номер билета: {t.barcode}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Куплен: {formatDateTimeRu(t.purchaseDate)}
                </Typography>
              </CardContent>
              <CardActions sx={{ px: 2, pb: 2 }}>
                <Button component={RouterLink} to={`/events/${t.eventId}`} size="small" variant="outlined">
                  Событие
                </Button>
                <Button component={RouterLink} to={`/tickets/${t.id}`} size="small" variant="contained">
                  Детали
                </Button>
              </CardActions>
            </Card>
          ))}
          {rows.length === 0 && !ticketsQuery.isLoading && (
            <Typography color="text.secondary">Пока нет билетов — выберите событие в каталоге.</Typography>
          )}
        </Stack>
      </Paper>
    </Stack>
  );
}
