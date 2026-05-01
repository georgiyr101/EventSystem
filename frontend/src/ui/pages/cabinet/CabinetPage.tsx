import { useEffect, useMemo, useState } from "react";
import { Box, Button, Card, CardContent, Stack, Tab, Tabs, TextField, Typography } from "@mui/material";
import { Link as RouterLink, Navigate, useSearchParams } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { authUpdateProfile } from "../../../api/auth";
import { listCategories } from "../../../api/categories";
import { createEvent, deleteEvent, listEvents, updateEvent } from "../../../api/events";
import { getOrganizerById } from "../../../api/organizers";
import { listTickets } from "../../../api/tickets";
import type { AppRole, EventRequestDto, EventResponseDto } from "../../../api/types";
import { useAuth } from "../../../auth/AuthContext";
import { ErrorAlert } from "../../components/ErrorAlert";
import { formatDateTimeRu, formatPriceBr } from "../../format";
import { EventFormDialog } from "../events/EventFormDialog";

function allowedTabs(role: AppRole): string[] {
  if (role === "ORGANIZER") return ["events", "create", "profile"];
  return ["tickets", "profile"];
}

function defaultTab(role: AppRole): string {
  return role === "ORGANIZER" ? "events" : "tickets";
}

function CabinetProfilePanel() {
  const { profile, setSession } = useAuth();
  const qc = useQueryClient();
  const [fullName, setFullName] = useState(profile?.fullName ?? "");
  const [email, setEmail] = useState(profile?.email ?? "");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");

  const mut = useMutation({
    mutationFn: authUpdateProfile,
    onSuccess: async (dto) => {
      setSession(dto);
      setCurrentPassword("");
      setNewPassword("");
      await qc.invalidateQueries();
    },
  });

  if (!profile) return null;

  return (
    <Stack spacing={2} sx={{ maxWidth: 480 }}>
      <Typography color="text.secondary">
        Обновите имя, email или пароль. Новый пароль не короче 8 символов; для смены пароля укажите текущий.
      </Typography>
      <ErrorAlert error={mut.error} />
      <TextField label="Имя" value={fullName} onChange={(e) => setFullName(e.target.value)} fullWidth required />
      <TextField
        label="Email"
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        fullWidth
        required
      />
      <TextField
        label="Текущий пароль"
        type="password"
        value={currentPassword}
        onChange={(e) => setCurrentPassword(e.target.value)}
        fullWidth
        autoComplete="current-password"
      />
      <TextField
        label="Новый пароль"
        type="password"
        value={newPassword}
        onChange={(e) => setNewPassword(e.target.value)}
        fullWidth
        autoComplete="new-password"
        helperText="Оставьте пустым, если не меняете пароль"
      />
      <Button
        variant="contained"
        disabled={mut.isPending || !fullName.trim() || !email.trim()}
        onClick={() =>
          mut.mutate({
            fullName: fullName.trim(),
            email: email.trim(),
            ...(newPassword ? { currentPassword, newPassword } : {}),
          })
        }
      >
        Сохранить
      </Button>
    </Stack>
  );
}

export function CabinetPage() {
  const { profile } = useAuth();
  const [sp, setSp] = useSearchParams();
  const tabParam = sp.get("tab");
  const tabs = profile ? allowedTabs(profile.role) : [];
  const activeTab =
    tabParam && tabs.includes(tabParam) ? tabParam : profile ? defaultTab(profile.role) : "tickets";

  if (!profile) return null;

  if (!tabParam || !tabs.includes(tabParam)) {
    return <Navigate to={`/cabinet?tab=${activeTab}`} replace />;
  }

  const handleTab = (_: React.SyntheticEvent, value: string) => {
    const next = new URLSearchParams(sp);
    next.set("tab", value);
    setSp(next, { replace: true });
  };

  return (
    <Stack spacing={3} sx={{ pb: 4 }}>
      <Box>
        <Typography variant="h4" sx={{ fontWeight: 750, letterSpacing: -0.3, mb: 0.5 }}>
          Личный кабинет
        </Typography>
        <Typography color="text.secondary">Управляйте билетами, событиями и данными профиля.</Typography>
      </Box>

      <Tabs
        value={activeTab}
        onChange={handleTab}
        sx={{
          borderBottom: 1,
          borderColor: "divider",
          minHeight: 44,
          "& .MuiTab-root": { textTransform: "none", fontWeight: 650, minHeight: 44 },
        }}
      >
        {(profile.role === "USER" || profile.role === "ADMIN") && <Tab label="Билеты" value="tickets" />}
        {profile.role === "ORGANIZER" && <Tab label="Мои события" value="events" />}
        {profile.role === "ORGANIZER" && <Tab label="Создать" value="create" />}
        <Tab label="Профиль" value="profile" />
      </Tabs>

      <Box sx={{ pt: 1 }}>
        {activeTab === "tickets" && (profile.role === "USER" || profile.role === "ADMIN") && (
          <CabinetTicketsPanel />
        )}
        {activeTab === "events" && profile.role === "ORGANIZER" && <CabinetOrganizerEventsPanel />}
        {activeTab === "create" && profile.role === "ORGANIZER" && <CabinetOrganizerCreatePanel />}
        {activeTab === "profile" && <CabinetProfilePanel />}
      </Box>
    </Stack>
  );
}

function CabinetTicketsPanel() {
  const ticketsQuery = useQuery({
    queryKey: ["tickets", "mine"],
    queryFn: () => listTickets(),
  });

  const rows = ticketsQuery.data ?? [];

  return (
    <Stack spacing={2}>
      <ErrorAlert error={ticketsQuery.error} />
      <Box
        sx={{
          display: "grid",
          gap: 2,
          gridTemplateColumns: { xs: "1fr", md: "repeat(2, 1fr)" },
        }}
      >
        {rows.map((t) => (
          <Card key={t.id} variant="outlined" sx={{ borderColor: "rgba(15,23,42,0.08)", borderRadius: 2 }}>
            <CardContent>
              <Stack spacing={0.75}>
                <Typography variant="h6" sx={{ lineHeight: 1.25 }}>
                  {t.eventName}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Дата события: {formatDateTimeRu(t.eventStartDate)}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Номер билета: {t.barcode}
                </Typography>
                <Button
                  component={RouterLink}
                  to={`/events/${t.eventId}`}
                  variant="text"
                  size="small"
                  sx={{ alignSelf: "flex-start", px: 0 }}
                >
                  Страница события
                </Button>
              </Stack>
            </CardContent>
          </Card>
        ))}
        {rows.length === 0 && !ticketsQuery.isLoading && (
          <Typography color="text.secondary" sx={{ gridColumn: "1 / -1" }}>
            Пока нет билетов — выберите событие в каталоге.
          </Typography>
        )}
      </Box>
    </Stack>
  );
}

function CabinetOrganizerEventsPanel() {
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

  const [edit, setEdit] = useState<{ id: number; dto: EventRequestDto } | null>(null);

  const categories = categoriesQuery.data ?? [];
  const organizersForForm = myOrganizer ? [myOrganizer] : [];
  const hasFormData = myOrganizer != null && categories.length > 0;

  return (
    <Stack spacing={2}>
      {!hasFormData && (
        <Typography color="text.secondary">
          Нужен профиль организатора и хотя бы одна категория. Категории создаёт администратор.
        </Typography>
      )}
      <ErrorAlert error={organizerQuery.error || eventsQuery.error || categoriesQuery.error} />
      <Box
        sx={{
          display: "grid",
          gap: 2,
          gridTemplateColumns: { xs: "1fr", md: "repeat(2, 1fr)" },
        }}
      >
        {rows.map((e) => (
          <Card key={e.id} variant="outlined" sx={{ borderColor: "rgba(15,23,42,0.08)", borderRadius: 2 }}>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 1 }}>
                {e.name}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {formatPriceBr(e.ticketPrice)} · Начало: {formatDateTimeRu(e.startDate)}
              </Typography>
            </CardContent>
            <Stack direction="row" flexWrap="wrap" gap={1} sx={{ px: 2, pb: 2 }}>
              <Button component={RouterLink} to={`/events/${e.id}`} variant="outlined" size="small">
                Открыть
              </Button>
              <Button
                variant="contained"
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
                Редактировать
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
            </Stack>
          </Card>
        ))}
        {rows.length === 0 && (
          <Typography color="text.secondary" sx={{ gridColumn: "1 / -1" }}>
            Пока нет событий — создайте на вкладке «Создать».
          </Typography>
        )}
      </Box>

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

function CabinetOrganizerCreatePanel() {
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

  const myOrganizer = organizerQuery.data;
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

  const createMut = useMutation({
    mutationFn: createEvent,
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["events"] });
      setCreateOpen(false);
    },
  });

  const [createOpen, setCreateOpen] = useState(false);

  useEffect(() => {
    setCreateOpen(true);
  }, []);

  return (
    <Stack spacing={2}>
      {!hasFormData && (
        <Typography color="text.secondary">
          Нужен профиль организатора и хотя бы одна категория в системе.
        </Typography>
      )}
      <ErrorAlert error={organizerQuery.error || categoriesQuery.error} />
      <Typography variant="body2" color="text.secondary">
        Заполните форму мероприятия в диалоге ниже. После сохранения вы можете открыть её снова.
      </Typography>
      <Button variant="outlined" onClick={() => setCreateOpen(true)} disabled={!hasFormData} sx={{ alignSelf: "flex-start" }}>
        Новое мероприятие
      </Button>

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
    </Stack>
  );
}
