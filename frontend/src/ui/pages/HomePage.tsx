import { useEffect, useRef, useState } from "react";
import {
  Autocomplete,
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  Chip,
  Divider,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import type { Dayjs } from "dayjs";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { listCategories } from "../../api/categories";
import { listEvents } from "../../api/events";
import type { CategoryResponseDto, EventResponseDto } from "../../api/types";
import { ErrorAlert } from "../components/ErrorAlert";
import { eventStatusLabel, formatDateTimeRu, formatPriceBr } from "../format";

const SEARCH_DEBOUNCE_MS = 400;

function buildEventsSearchParams(opts: {
  q: string;
  categories: CategoryResponseDto[];
  dateFrom: Dayjs | null;
  minPrice: string;
  maxPrice: string;
}): string {
  const sp = new URLSearchParams();
  if (opts.q.trim()) sp.set("q", opts.q.trim());
  for (const c of opts.categories) sp.append("category", c.name);
  if (opts.dateFrom?.isValid()) sp.set("dateFrom", opts.dateFrom.format("YYYY-MM-DD"));
  if (opts.minPrice) sp.set("minPrice", opts.minPrice);
  if (opts.maxPrice) sp.set("maxPrice", opts.maxPrice);
  sp.set("page", "1");
  return sp.toString();
}

export function HomePage() {
  const navigate = useNavigate();
  const qDebounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const filtersRef = useRef({
    selectedCategories: [] as CategoryResponseDto[],
    dateFrom: null as Dayjs | null,
    minPrice: "",
    maxPrice: "",
  });

  const [q, setQ] = useState("");
  const [selectedCategories, setSelectedCategories] = useState<CategoryResponseDto[]>([]);
  const [dateFrom, setDateFrom] = useState<Dayjs | null>(null);
  const [minPrice, setMinPrice] = useState("");
  const [maxPrice, setMaxPrice] = useState("");

  filtersRef.current = { selectedCategories, dateFrom, minPrice, maxPrice };

  const clearSearchDebounce = () => {
    if (qDebounceRef.current) {
      clearTimeout(qDebounceRef.current);
      qDebounceRef.current = null;
    }
  };

  useEffect(() => () => clearSearchDebounce(), []);

  const goToEvents = (opts: {
    q: string;
    categories: CategoryResponseDto[];
    dateFrom: Dayjs | null;
    minPrice: string;
    maxPrice: string;
  }) => {
    navigate(`/events?${buildEventsSearchParams(opts)}`, { replace: true });
  };

  const categoriesQuery = useQuery({
    queryKey: ["categories", "all"],
    queryFn: () => listCategories(),
  });

  const eventsQuery = useQuery({
    queryKey: ["events", "home-preview"],
    queryFn: listEvents,
  });

  const categories = categoriesQuery.data ?? [];
  const baseRows: EventResponseDto[] = eventsQuery.data ?? [];
  const preview = baseRows.slice(0, 6);

  const eventsListHref = `/events?${buildEventsSearchParams({
    q,
    categories: selectedCategories,
    dateFrom,
    minPrice,
    maxPrice,
  })}`;

  const resetFilters = () => {
    clearSearchDebounce();
    setQ("");
    setSelectedCategories([]);
    setDateFrom(null);
    setMinPrice("");
    setMaxPrice("");
  };

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h4" sx={{ mb: 0.5 }}>
          Найдите мероприятие за несколько секунд
        </Typography>
        <Typography color="text.secondary" sx={{ maxWidth: 760 }}>
          Укажите условия — откроется каталог с теми же фильтрами.
        </Typography>
      </Box>

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
          onChange={(e) => {
            const v = e.target.value;
            setQ(v);
            clearSearchDebounce();
            qDebounceRef.current = setTimeout(() => {
              const f = filtersRef.current;
              goToEvents({
                q: v,
                categories: f.selectedCategories,
                dateFrom: f.dateFrom,
                minPrice: f.minPrice,
                maxPrice: f.maxPrice,
              });
              qDebounceRef.current = null;
            }, SEARCH_DEBOUNCE_MS);
          }}
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
          onChange={(_, v) => {
            clearSearchDebounce();
            setSelectedCategories(v);
            goToEvents({ q, categories: v, dateFrom, minPrice, maxPrice });
          }}
          renderInput={(params) => <TextField {...params} label="Категории" placeholder="IT, Music…" />}
          sx={{ flex: "1 1 220px", minWidth: 200, maxWidth: 360 }}
        />
        <DatePicker
          label="Начиная с даты"
          value={dateFrom}
          onChange={(v) => {
            clearSearchDebounce();
            setDateFrom(v);
            goToEvents({ q, categories: selectedCategories, dateFrom: v, minPrice, maxPrice });
          }}
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
          onChange={(e) => {
            const v = e.target.value;
            clearSearchDebounce();
            setMinPrice(v);
            goToEvents({ q, categories: selectedCategories, dateFrom, minPrice: v, maxPrice });
          }}
          sx={{ width: 112 }}
        />
        <TextField
          size="small"
          label="Макс. цена"
          type="number"
          value={maxPrice}
          onChange={(e) => {
            const v = e.target.value;
            clearSearchDebounce();
            setMaxPrice(v);
            goToEvents({ q, categories: selectedCategories, dateFrom, minPrice, maxPrice: v });
          }}
          sx={{ width: 112 }}
        />
        <Button size="small" variant="text" color="inherit" onClick={resetFilters} sx={{ flexShrink: 0 }}>
          Сбросить
        </Button>
      </Stack>

      <ErrorAlert error={eventsQuery.error || categoriesQuery.error} />

      <Box>
        <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
          <Typography variant="h5">Рекомендуем из каталога</Typography>
          <Button component={RouterLink} to={eventsListHref} variant="text">
            Смотреть все
          </Button>
        </Stack>
        <Divider sx={{ mb: 2 }} />
        <Box
          sx={{
            display: "grid",
            gap: 2,
            gridTemplateColumns: { xs: "1fr", sm: "repeat(2, 1fr)", md: "repeat(3, 1fr)" },
          }}
        >
          {preview.map((e) => (
            <Card
              key={e.id}
              variant="outlined"
              sx={{
                height: "100%",
                display: "flex",
                flexDirection: "column",
                borderColor: "rgba(15,23,42,0.08)",
              }}
            >
              <CardContent sx={{ flex: "1 1 auto", display: "flex", flexDirection: "column" }}>
                <Stack spacing={1} sx={{ flex: "1 1 auto" }}>
                  <Stack direction="row" justifyContent="space-between" alignItems="flex-start" gap={1}>
                    <Typography
                      variant="h6"
                      sx={{
                        lineHeight: 1.25,
                        flex: "1 1 auto",
                        minWidth: 0,
                        display: "-webkit-box",
                        WebkitLineClamp: 3,
                        WebkitBoxOrient: "vertical",
                        overflow: "hidden",
                      }}
                    >
                      {e.name}
                    </Typography>
                    <Chip size="small" label={eventStatusLabel(e)} sx={{ flexShrink: 0 }} />
                  </Stack>
                  <Typography variant="body2" color="text.secondary">
                    {e.organizerName}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {formatPriceBr(e.ticketPrice)} · {e.startDate ? formatDateTimeRu(e.startDate) : "дата уточняется"}
                  </Typography>
                </Stack>
              </CardContent>
              <CardActions sx={{ px: 2, pb: 2, pt: 0, mt: "auto" }}>
                <Button component={RouterLink} to={`/events/${e.id}`} variant="contained" fullWidth>
                  Подробнее
                </Button>
              </CardActions>
            </Card>
          ))}
          {preview.length === 0 && !eventsQuery.isLoading && (
            <Typography color="text.secondary" sx={{ gridColumn: "1 / -1" }}>
              Событий пока нет — загляните позже.
            </Typography>
          )}
        </Box>
      </Box>
    </Stack>
  );
}
