import { useMemo, useState } from "react";
import {
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  Chip,
  Divider,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import { Link as RouterLink } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { listCategories } from "../../../api/categories";
import { createEvent, deleteEvent, listEvents, updateEvent } from "../../../api/events";
import { getOrganizerById } from "../../../api/organizers";
import type { EventRequestDto, EventResponseDto } from "../../../api/types";
import { useAuth } from "../../../auth/AuthContext";
import { ErrorAlert } from "../../components/ErrorAlert";
import { eventStatusLabel, formatDateTimeRu, formatPriceBr } from "../../format";
import { EventFormDialog } from "../events/EventFormDialog";

export function OrganizerEventsPage() {
  const { profile } = useAuth();
  const organizerId = profile?.organizerId ?? null;
  const qc = useQueryClient();

  const organizerQuery = useQuery({
    queryKey: ["organizers", "mine", organizerId],
    queryFn: () => getOrganizerById(organizerId!),
    enabled: organizerId != null && organizerId > 0,
  });

  const categoriesQuery = useQuery({
    queryKey: ["categories", "all"],
    queryFn: () => listCategories(),
  });

  const eventsQuery = useQuery({
    queryKey: ["events"],
    queryFn: listEvents,
  });

  const orgName = organizerQuery.data?.name;
  const myOrganizer = organizerQuery.data;

  const rows: EventResponseDto[] = useMemo(() => {
    const all = eventsQuery.data ?? [];
    if (!orgName) return [];
    return all.filter((e) => e.organizerName === orgName);
  }, [eventsQuery.data, orgName]);

  const createMut = useMutation({
    mutationFn: createEvent,
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["events"] });
      setCreateOpen(false);
    },
  });

  const updateMut = useMutation({
    mutationFn: ({ id, dto }: { id: number; dto: EventRequestDto }) => updateEvent(id, dto),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["events"] });
      setEdit(null);
    },
  });

  const deleteMut = useMutation({
    mutationFn: deleteEvent,
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["events"] });
    },
  });

  const [createOpen, setCreateOpen] = useState(false);
  const [edit, setEdit] = useState<{ id: number; dto: EventRequestDto } | null>(null);

  const categories = categoriesQuery.data ?? [];
  const organizersForForm = myOrganizer ? [myOrganizer] : [];
  const hasFormData = myOrganizer != null && categories.length > 0;

  const defaultDto = useMemo((): Partial<EventRequestDto> | undefined => {
    if (!myOrganizer) return undefined;
    const start = new Date();
    start.setDate(start.getDate() + 1);
    const end = new Date(start);
    end.setHours(end.getHours() + 2);
    const pad = (n: number) => String(n).padStart(2, "0");
    const toLocal = (d: Date) =>
      `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00`;
    return {
      organizerId: myOrganizer.id,
      categoryIds: categories[0]?.id ? [categories[0].id] : [],
      startDate: toLocal(start),
      endDate: toLocal(end),
      maxParticipants: 50,
      ticketPrice: 0,
    };
  }, [myOrganizer, categories]);

  return (
    <Stack spacing={2}>
      <Stack direction={{ xs: "column", sm: "row" }} justifyContent="space-between" alignItems={{ sm: "center" }}>
        <Box>
          <Typography variant="h4">Кабинет организатора</Typography>
          <Typography color="text.secondary">
            События вашего профиля организатора.
          </Typography>
        </Box>
        <Button variant="contained" onClick={() => setCreateOpen(true)} disabled={!hasFormData}>
          Новое событие
        </Button>
      </Stack>

      {!hasFormData && (
        <Paper sx={{ p: 2 }}>
          <Typography color="text.secondary">
            Нужен профиль организатора и хотя бы одна категория в системе. Категории создаёт администратор.
          </Typography>
        </Paper>
      )}

      <ErrorAlert error={organizerQuery.error || eventsQuery.error || categoriesQuery.error} />

      <Paper sx={{ p: 2 }}>
        <Typography variant="subtitle1" sx={{ mb: 1 }}>
          Мои события
        </Typography>
        <Divider sx={{ mb: 2 }} />
        <Box
          sx={{
            display: "grid",
            gap: 2,
            gridTemplateColumns: { xs: "1fr", md: "repeat(2, 1fr)" },
          }}
        >
          {rows.map((e) => (
            <Card key={e.id} variant="outlined" sx={{ height: "100%", borderColor: "rgba(15,23,42,0.08)" }}>
                <CardContent>
                  <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 1 }}>
                    <Typography variant="h6">{e.name}</Typography>
                    <Chip size="small" label={eventStatusLabel(e)} />
                  </Stack>
                  <Typography variant="body2" color="text.secondary">
                    {formatPriceBr(e.ticketPrice)} · Начало: {formatDateTimeRu(e.startDate)}
                  </Typography>
                </CardContent>
                <CardActions sx={{ px: 2, pb: 2, flexWrap: "wrap", gap: 1 }}>
                  <Button component={RouterLink} to={`/events/${e.id}`} variant="contained" size="small">
                    Открыть
                  </Button>
                  <Button
                    variant="outlined"
                    size="small"
                    disabled={!hasFormData}
                    onClick={() =>
                      setEdit({
                        id: e.id,
                        dto: {
                          name: e.name,
                          startDate: e.startDate ?? "",
                          endDate: e.startDate ?? "",
                          maxParticipants: 50,
                          ticketPrice: e.ticketPrice,
                          organizerId: myOrganizer?.id ?? 0,
                          categoryIds: categories.filter((c) => (e.categoryNames ?? []).includes(c.name)).map((c) => c.id),
                        },
                      })
                    }
                  >
                    Изменить
                  </Button>
                  <Button
                    color="error"
                    variant="text"
                    size="small"
                    disabled={deleteMut.isPending}
                    onClick={() => deleteMut.mutate(e.id)}
                  >
                    Удалить
                  </Button>
                </CardActions>
              </Card>
          ))}
          {rows.length === 0 && (
            <Typography color="text.secondary" sx={{ gridColumn: "1 / -1" }}>
              Пока нет событий — создайте первое.
            </Typography>
          )}
        </Box>
      </Paper>

      <EventFormDialog
        open={createOpen}
        title="Новое событие"
        organizers={organizersForForm}
        categories={categories}
        initial={defaultDto ?? undefined}
        onClose={() => setCreateOpen(false)}
        onSubmit={(dto) => createMut.mutate(dto)}
        busy={createMut.isPending}
        submitLabel="Создать"
      />

      <EventFormDialog
        open={!!edit}
        title="Редактирование"
        organizers={organizersForForm}
        categories={categories}
        initial={edit?.dto ?? undefined}
        onClose={() => setEdit(null)}
        onSubmit={(dto) => edit && updateMut.mutate({ id: edit.id, dto })}
        busy={updateMut.isPending}
        submitLabel="Сохранить"
      />
    </Stack>
  );
}
