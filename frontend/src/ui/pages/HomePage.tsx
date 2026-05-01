import { useState } from "react";
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
import type { Dayjs } from "dayjs";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { listCategories } from "../../api/categories";
import { listEvents } from "../../api/events";
import type { CategoryResponseDto } from "../../api/types";
import { ErrorAlert } from "../components/ErrorAlert";
import { eventStatusLabel, formatDateTimeRu, formatPriceBr } from "../format";

export function HomePage() {
  const nav = useNavigate();
  const [q, setQ] = useState("");
  const [selectedCategories, setSelectedCategories] = useState<CategoryResponseDto[]>([]);
  const [dateFrom, setDateFrom] = useState<Dayjs | null>(null);
  const [minPrice, setMinPrice] = useState("");
  const [maxPrice, setMaxPrice] = useState("");

  const categoriesQuery = useQuery({
    queryKey: ["categories", "all"],
    queryFn: () => listCategories(),
  });

  const eventsQuery = useQuery({
    queryKey: ["events", "home-preview"],
    queryFn: listEvents,
  });

  const preview = (eventsQuery.data ?? []).slice(0, 6);

  return (
    <Stack spacing={3}>
      <Paper
        sx={{
          p: { xs: 2.5, sm: 4 },
          overflow: "hidden",
          position: "relative",
          background:
            "radial-gradient(1200px circle at 10% 10%, rgba(27,110,243,0.22), transparent 40%)," +
            "radial-gradient(900px circle at 90% 10%, rgba(124,58,237,0.18), transparent 42%)," +
            "linear-gradient(180deg, #FFFFFF 0%, #F7F9FF 100%)",
          border: "1px solid rgba(15,23,42,0.06)",
        }}
      >
        <Stack spacing={2}>
          <Typography variant="h4">Найдите мероприятие за несколько секунд</Typography>
          <Typography color="text.secondary" sx={{ maxWidth: 760 }}>
            Введите название события и/или укажите фильтры — вы попадёте на страницу Events с результатами.
          </Typography>

          <Stack
            spacing={2}
            sx={{ mt: 1 }}
            component="form"
            onSubmit={(e) => {
              e.preventDefault();
              const sp = new URLSearchParams();
              if (q.trim()) sp.set("q", q.trim());
              for (const c of selectedCategories) sp.append("category", c.name);
              if (dateFrom?.isValid()) sp.set("dateFrom", dateFrom.format("YYYY-MM-DD"));
              if (minPrice) sp.set("minPrice", minPrice);
              if (maxPrice) sp.set("maxPrice", maxPrice);
              nav(`/events?${sp.toString()}`);
            }}
          >
            <Box
              sx={{
                display: "flex",
                alignItems: "stretch",
                width: "100%",
              }}
            >
              <TextField
                label="Поиск по названию"
                value={q}
                onChange={(e) => setQ(e.target.value)}
                placeholder="Название мероприятия…"
                sx={{
                  flex: 1,
                  "& .MuiOutlinedInput-root": {
                    borderTopRightRadius: 0,
                    borderBottomRightRadius: 0,
                  },
                  "& .MuiOutlinedInput-root fieldset": {
                    borderRight: "none",
                  },
                  "& .MuiOutlinedInput-root:hover fieldset": {
                    borderRight: "none",
                  },
                  "& .MuiOutlinedInput-root.Mui-focused fieldset": {
                    borderRightWidth: 1,
                    borderRightStyle: "solid",
                  },
                }}
              />
              <Button
                variant="contained"
                type="submit"
                disableElevation
                size="large"
                sx={{
                  borderTopLeftRadius: 0,
                  borderBottomLeftRadius: 0,
                  px: 3,
                  minWidth: 112,
                  alignSelf: "stretch",
                }}
              >
                Найти
              </Button>
            </Box>

            <Box
              sx={{
                display: "grid",
                gap: 1.5,
                gridTemplateColumns: { xs: "1fr", sm: "repeat(2, 1fr)" },
              }}
            >
              <Autocomplete
                multiple
                options={categoriesQuery.data ?? []}
                getOptionLabel={(o) => o.name}
                loading={categoriesQuery.isLoading}
                value={selectedCategories}
                onChange={(_, v) => setSelectedCategories(v)}
                renderInput={(params) => (
                  <TextField {...params} label="Категории" placeholder="Несколько категорий…" />
                )}
              />
              <DatePicker
                label="Начиная с даты"
                value={dateFrom}
                onChange={(v) => setDateFrom(v)}
                slotProps={{ textField: { fullWidth: true } }}
              />
              <Stack
                direction="row"
                spacing={1.5}
                sx={{ gridColumn: { xs: "1 / -1", sm: "1 / -1" } }}
              >
                <TextField
                  label="Цена от"
                  type="number"
                  value={minPrice}
                  onChange={(e) => setMinPrice(e.target.value)}
                  sx={{ flex: 1 }}
                />
                <TextField
                  label="Цена до"
                  type="number"
                  value={maxPrice}
                  onChange={(e) => setMaxPrice(e.target.value)}
                  sx={{ flex: 1 }}
                />
              </Stack>
              <Button
                type="button"
                variant="text"
                color="inherit"
                onClick={() => {
                  setQ("");
                  setSelectedCategories([]);
                  setDateFrom(null);
                  setMinPrice("");
                  setMaxPrice("");
                }}
                sx={{ justifySelf: "start", gridColumn: "1 / -1" }}
              >
                Сбросить
              </Button>
            </Box>
          </Stack>
        </Stack>
      </Paper>

      <ErrorAlert error={eventsQuery.error || categoriesQuery.error} />

      <Box>
        <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
          <Typography variant="h5">Рекомендуем из каталога</Typography>
          <Button component={RouterLink} to="/events" variant="text">
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
            <Card key={e.id} variant="outlined" sx={{ height: "100%", borderColor: "rgba(15,23,42,0.08)" }}>
              <CardContent>
                <Stack spacing={1}>
                  <Stack direction="row" justifyContent="space-between" alignItems="center">
                    <Typography variant="h6" sx={{ lineHeight: 1.25 }}>
                      {e.name}
                    </Typography>
                    <Chip size="small" label={eventStatusLabel(e)} />
                  </Stack>
                  <Typography variant="body2" color="text.secondary">
                    {e.organizerName}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {formatPriceBr(e.ticketPrice)} · {e.startDate ? formatDateTimeRu(e.startDate) : "дата уточняется"}
                  </Typography>
                </Stack>
              </CardContent>
              <CardActions sx={{ px: 2, pb: 2 }}>
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
