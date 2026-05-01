import React from "react";
import {
  Autocomplete,
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  Chip,
  Divider,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import dayjs, { type Dayjs } from "dayjs";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link as RouterLink, useSearchParams } from "react-router-dom";
import type { EventRequestDto, EventResponseDto } from "../../../api/types";
import { createEvent, deleteEvent, listEvents, updateEvent } from "../../../api/events";
import { listOrganizers } from "../../../api/organizers";
import { listCategories } from "../../../api/categories";
import { ErrorAlert } from "../../components/ErrorAlert";
import { eventStatusLabel, formatDateTimeRu, formatPriceBr } from "../../format";
import { EventFormDialog } from "./EventFormDialog";
import { useAuth } from "../../../auth/AuthContext";

function parseDateOnly(v: string): Date | null {
  const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(v.trim());
  if (!m) return null;
  const d = new Date(Number(m[1]), Number(m[2]) - 1, Number(m[3]));
  return Number.isNaN(d.getTime()) ? null : d;
}

function startOfDay(d: Date) {
  const copy = new Date(d);
  copy.setHours(0, 0, 0, 0);
  return copy;
}

function useUrlState() {
  const [sp, setSp] = useSearchParams();
  const get = (k: string) => sp.get(k) ?? "";
  const set = (k: string, v: string) => {
    const next = new URLSearchParams(sp);
    if (!v) next.delete(k);
    else next.set(k, v);
    setSp(next, { replace: true });
  };
  const getAll = (k: string) => sp.getAll(k).filter(Boolean);
  const setMany = (k: string, values: string[]) => {
    const next = new URLSearchParams(sp);
    next.delete(k);
    for (const v of values) {
      if (v) next.append(k, v);
    }
    setSp(next, { replace: true });
  };
  return { get, set, getAll, setMany };
}

export function EventsPage() {
  const qc = useQueryClient();
  const { profile } = useAuth();
  const isAdmin = profile?.role === "ADMIN";
  const { get, set, getAll, setMany } = useUrlState();

  const q = get("q");
  const categoryNames = getAll("category");
  const categoriesUrlKey = [...categoryNames].sort().join("\0");
  const minPrice = get("minPrice");
  const maxPrice = get("maxPrice");
  const dateFrom = get("dateFrom");

  const organizersQuery = useQuery({
    queryKey: ["organizers", "all"],
    queryFn: listOrganizers,
    enabled: isAdmin,
  });
  const categoriesQuery = useQuery({
    queryKey: ["categories", "all"],
    queryFn: () => listCategories(),
  });

  const eventsQuery = useQuery({
    queryKey: ["events", "list"],
    queryFn: listEvents,
  });

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

  const [createOpen, setCreateOpen] = React.useState(false);
  const [edit, setEdit] = React.useState<{ id: number; dto: EventRequestDto } | null>(null);

  const baseRows: EventResponseDto[] = eventsQuery.data ?? [];

  const filteredRows = React.useMemo(() => {
    const qNorm = q.trim().toLowerCase();
    const minPriceNum = minPrice ? Number(minPrice) : null;
    const maxPriceNum = maxPrice ? Number(maxPrice) : null;
    const from = dateFrom ? parseDateOnly(dateFrom) : null;
    const namesFromUrl = categoriesUrlKey ? categoriesUrlKey.split("\0") : [];
    const categoryFilterLower = new Set(namesFromUrl.map((n) => n.toLowerCase()));

    return baseRows.filter((e) => {
      if (qNorm && !e.name.toLowerCase().includes(qNorm)) return false;

      if (categoryFilterLower.size > 0) {
        const eventCats = new Set((e.categoryNames ?? []).map((n) => n.toLowerCase()));
        const matchesAll = [...categoryFilterLower].every((sel) => eventCats.has(sel));
        if (!matchesAll) return false;
      }

      if (minPriceNum != null && Number.isFinite(minPriceNum) && e.ticketPrice < minPriceNum) return false;
      if (maxPriceNum != null && Number.isFinite(maxPriceNum) && e.ticketPrice > maxPriceNum) return false;

      if (from) {
        if (!e.startDate) return false;
        const start = new Date(e.startDate);
        if (Number.isNaN(start.getTime())) return false;
        if (start < startOfDay(from)) return false;
      }
      return true;
    });
  }, [baseRows, q, minPrice, maxPrice, dateFrom, categoriesUrlKey]);

  const hasLookupData =
    isAdmin && (organizersQuery.data?.length ?? 0) > 0 && (categoriesQuery.data?.length ?? 0) > 0;

  const categories = categoriesQuery.data ?? [];
  const selectedCategories = React.useMemo(() => {
    const namesFromUrl = categoriesUrlKey ? categoriesUrlKey.split("\0") : [];
    const wanted = new Set(namesFromUrl.map((n) => n.toLowerCase()));
    return categories.filter((c) => wanted.has(c.name.toLowerCase()));
  }, [categories, categoriesUrlKey]);

  const datePickerValue: Dayjs | null = dateFrom ? dayjs(dateFrom) : null;

  const resetFilters = () => {
    set("q", "");
    setMany("category", []);
    set("minPrice", "");
    set("maxPrice", "");
    set("dateFrom", "");
  };

  return (
    <Stack spacing={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="flex-start" flexWrap="wrap" gap={1}>
        <Box>
          <Typography variant="h4">Мероприятия</Typography>
          <Typography color="text.secondary">Каталог событий и удобные фильтры.</Typography>
        </Box>
        {isAdmin && (
          <Button variant="contained" onClick={() => setCreateOpen(true)} disabled={!hasLookupData}>
            Создать
          </Button>
        )}
      </Stack>

      <Stack
        direction="row"
        flexWrap="wrap"
        alignItems="center"
        gap={1.5}
        sx={{ rowGap: 1.5 }}
        useFlexGap
      >
        <TextField
          size="small"
          label="Поиск событий"
          value={q}
          onChange={(e) => set("q", e.target.value)}
          placeholder="Название…"
          sx={{ flex: "1 1 160px", minWidth: 140 }}
        />
        <Autocomplete
          multiple
          size="small"
          options={categories}
          getOptionLabel={(o) => o.name}
          loading={categoriesQuery.isLoading}
          value={selectedCategories}
          onChange={(_, v) => setMany("category", v.map((c) => c.name))}
          renderInput={(params) => <TextField {...params} label="Категории" placeholder="IT, Music…" />}
          sx={{ flex: "1 1 220px", minWidth: 200, maxWidth: 360 }}
        />
        <DatePicker
          label="Начиная с даты"
          value={datePickerValue}
          onChange={(v) => set("dateFrom", v && v.isValid() ? v.format("YYYY-MM-DD") : "")}
          slotProps={{
            textField: {
              size: "small",
              InputLabelProps: { shrink: true },
              sx: {
                flex: "0 1 auto",
                width: { xs: "100%", sm: 260 },
                minWidth: { sm: 260 },
              },
            },
          }}
        />
        <TextField
          size="small"
          label="Мин. цена"
          type="number"
          value={minPrice}
          onChange={(e) => set("minPrice", e.target.value)}
          sx={{ width: 112 }}
        />
        <TextField
          size="small"
          label="Макс. цена"
          type="number"
          value={maxPrice}
          onChange={(e) => set("maxPrice", e.target.value)}
          sx={{ width: 112 }}
        />
        <Button size="small" variant="text" color="inherit" onClick={resetFilters} sx={{ flexShrink: 0 }}>
          Сбросить
        </Button>
      </Stack>

      {isAdmin && !hasLookupData && (
        <Paper sx={{ p: 2 }}>
          <Typography color="text.secondary">
            Чтобы создавать и редактировать мероприятия, в системе должны быть хотя бы один организатор и одна категория.
          </Typography>
        </Paper>
      )}

      <ErrorAlert error={eventsQuery.error || organizersQuery.error || categoriesQuery.error} />

      <Paper sx={{ p: 2 }}>
        <Stack spacing={1}>
          <Typography variant="subtitle1">Найденные мероприятия</Typography>
          <Divider />
          <Box
            sx={{
              mt: 0,
              display: "grid",
              gap: 2,
              gridTemplateColumns: { xs: "1fr", md: "repeat(2, 1fr)" },
            }}
          >
            {filteredRows.map((e) => (
              <Card key={e.id} variant="outlined" sx={{ height: "100%", borderColor: "rgba(15,23,42,0.08)" }}>
                <CardContent>
                  <Stack spacing={1}>
                    <Stack direction="row" spacing={1} alignItems="center" justifyContent="space-between">
                      <Typography variant="h6" sx={{ lineHeight: 1.2 }}>
                        {e.name}
                      </Typography>
                      <Chip size="small" label={eventStatusLabel(e)} />
                    </Stack>
                    <Typography variant="body2" color="text.secondary">
                      Организатор: {e.organizerName}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {formatPriceBr(e.ticketPrice)} · {formatDateTimeRu(e.startDate)}
                    </Typography>
                    <Typography
                      variant="body2"
                      color="text.secondary"
                      sx={{
                        display: "-webkit-box",
                        WebkitLineClamp: 2,
                        WebkitBoxOrient: "vertical",
                        overflow: "hidden",
                      }}
                    >
                      Категории: {(e.categoryNames ?? []).join(", ") || "—"}
                    </Typography>
                  </Stack>
                </CardContent>
                <CardActions sx={{ px: 2, pb: 2 }}>
                  <Button component={RouterLink} to={`/events/${e.id}`} variant="contained">
                    Открыть
                  </Button>
                  {isAdmin && (
                    <>
                      <Button
                        variant="outlined"
                        onClick={() =>
                          setEdit({
                            id: e.id,
                            dto: {
                              name: e.name,
                              startDate: e.startDate ?? "",
                              endDate: e.startDate ?? "",
                              maxParticipants: 1,
                              ticketPrice: e.ticketPrice,
                              organizerId: organizersQuery.data?.find((o) => o.name === e.organizerName)?.id ?? 0,
                              categoryIds: (categoriesQuery.data ?? [])
                                .filter((c) => (e.categoryNames ?? []).includes(c.name))
                                .map((c) => c.id),
                            },
                          })
                        }
                        disabled={!hasLookupData}
                      >
                        Редактировать
                      </Button>
                      <Button
                        color="error"
                        variant="text"
                        onClick={() => deleteMut.mutate(e.id)}
                        disabled={deleteMut.isPending}
                        sx={{ marginLeft: "auto" }}
                      >
                        Удалить
                      </Button>
                    </>
                  )}
                </CardActions>
              </Card>
            ))}
            {filteredRows.length === 0 && (
              <Typography color="text.secondary" sx={{ gridColumn: "1 / -1" }}>
                Ничего не найдено.
              </Typography>
            )}
          </Box>
        </Stack>
      </Paper>

      {isAdmin && (
        <>
          <EventFormDialog
            open={createOpen}
            title="Новое мероприятие"
            organizers={organizersQuery.data ?? []}
            categories={categoriesQuery.data ?? []}
            onClose={() => setCreateOpen(false)}
            onSubmit={(dto) => createMut.mutate(dto)}
            busy={createMut.isPending}
            submitLabel="Создать"
          />

          <EventFormDialog
            open={!!edit}
            title="Редактирование мероприятия"
            organizers={organizersQuery.data ?? []}
            categories={categoriesQuery.data ?? []}
            initial={edit?.dto ?? undefined}
            onClose={() => setEdit(null)}
            onSubmit={(dto) => edit && updateMut.mutate({ id: edit.id, dto })}
            busy={updateMut.isPending}
            submitLabel="Сохранить"
          />
        </>
      )}
    </Stack>
  );
}
