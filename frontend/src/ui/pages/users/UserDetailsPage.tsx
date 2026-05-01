import { useParams, Link as RouterLink } from "react-router-dom";
import { Alert, Button, Divider, Paper, Stack, Typography } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { getUserById } from "../../../api/users";
import { listTickets } from "../../../api/tickets";
import type { TicketResponseDto } from "../../../api/types";
import { ErrorAlert } from "../../components/ErrorAlert";
import { formatDateTimeRu } from "../../format";

export function UserDetailsPage() {
  const { id } = useParams();
  const userId = Number(id);

  const userQuery = useQuery({
    queryKey: ["users", "byId", userId],
    queryFn: () => getUserById(userId),
    enabled: Number.isFinite(userId) && userId > 0,
  });

  const ticketsQuery = useQuery({
    queryKey: ["tickets", "byUser", userId],
    queryFn: () => listTickets({ userId }),
    enabled: Number.isFinite(userId) && userId > 0,
  });

  const tickets: TicketResponseDto[] = ticketsQuery.data ?? [];
  const heading = userQuery.data
    ? [userQuery.data.fullName?.trim(), userQuery.data.email].filter(Boolean).join(" · ") || userQuery.data.email
    : "Пользователь";

  return (
    <Stack spacing={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="center">
        <Typography variant="h4">{heading}</Typography>
        <Button component={RouterLink} to="/users" variant="outlined">
          Назад
        </Button>
      </Stack>

      <ErrorAlert error={userQuery.error || ticketsQuery.error} />
      {!userQuery.data && userQuery.isLoading && <Alert severity="info">Загрузка…</Alert>}

      {userQuery.data && (
        <Paper sx={{ p: 2 }}>
          <Stack spacing={1}>
            <Typography variant="h6">{userQuery.data.fullName ?? "—"}</Typography>
            <Typography color="text.secondary">Email: {userQuery.data.email}</Typography>
          </Stack>
        </Paper>
      )}

      <Paper sx={{ p: 2 }}>
        <Stack spacing={1}>
          <Typography variant="h6">Билеты пользователя</Typography>
          <Divider />
          {tickets.map((t) => (
            <Paper key={t.id} variant="outlined" sx={{ p: 1.5 }}>
              <Typography fontWeight={600}>{t.eventName}</Typography>
              <Typography variant="body2" color="text.secondary">
                Номер билета: {t.barcode}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Куплен: {formatDateTimeRu(t.purchaseDate)}
              </Typography>
            </Paper>
          ))}
          {tickets.length === 0 && <Typography color="text.secondary">У пользователя пока нет билетов.</Typography>}
        </Stack>
      </Paper>
    </Stack>
  );
}
