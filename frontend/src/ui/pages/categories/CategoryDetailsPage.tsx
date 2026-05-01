import { useParams, Link as RouterLink } from "react-router-dom";
import { Alert, Button, Divider, Paper, Stack, Typography } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { getCategoryById } from "../../../api/categories";
import { listEvents } from "../../../api/events";
import type { EventResponseDto } from "../../../api/types";
import { ErrorAlert } from "../../components/ErrorAlert";
import { eventStatusLabel, formatDateTimeRu, formatPriceBr } from "../../format";

export function CategoryDetailsPage() {
  const { id } = useParams();
  const categoryId = Number(id);

  const categoryQuery = useQuery({
    queryKey: ["categories", "byId", categoryId],
    queryFn: () => getCategoryById(categoryId),
    enabled: Number.isFinite(categoryId) && categoryId > 0,
  });

  const eventsQuery = useQuery({
    queryKey: ["events", "byCategoryName", categoryId],
    queryFn: async () => {
      const all = await listEvents();
      const categoryName = categoryQuery.data?.name ?? "";
      return categoryName ? all.filter((e) => (e.categoryNames ?? []).includes(categoryName)) : [];
    },
    enabled: !!categoryQuery.data?.name,
  });

  const events: EventResponseDto[] = eventsQuery.data ?? [];
  const title = categoryQuery.data?.name ?? "Категория";

  return (
    <Stack spacing={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="center">
        <Typography variant="h4">{title}</Typography>
        <Button component={RouterLink} to="/categories" variant="outlined">
          Назад
        </Button>
      </Stack>

      <ErrorAlert error={categoryQuery.error || eventsQuery.error} />
      {!categoryQuery.data && categoryQuery.isLoading && <Alert severity="info">Загрузка…</Alert>}

      <Paper sx={{ p: 2 }}>
        <Stack spacing={1}>
          <Typography variant="h6">Мероприятия в категории</Typography>
          <Divider />
          {events.map((e) => (
            <Paper key={e.id} variant="outlined" sx={{ p: 1.5 }}>
              <Typography fontWeight={600}>{e.name}</Typography>
              <Typography variant="body2" color="text.secondary">
                {e.organizerName} · {eventStatusLabel(e)} · {formatPriceBr(e.ticketPrice)}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Начало: {formatDateTimeRu(e.startDate)}
              </Typography>
              <Button component={RouterLink} to={`/events/${e.id}`} size="small" sx={{ mt: 1 }}>
                Открыть
              </Button>
            </Paper>
          ))}
          {events.length === 0 && <Typography color="text.secondary">В этой категории пока нет событий.</Typography>}
        </Stack>
      </Paper>
    </Stack>
  );
}
