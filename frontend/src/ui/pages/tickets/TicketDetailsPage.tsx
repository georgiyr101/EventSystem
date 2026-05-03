import { useState } from "react";
import { useParams, Link as RouterLink, useNavigate } from "react-router-dom";
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { deleteTicket, getTicketById } from "../../../api/tickets";
import { useAuth } from "../../../auth/AuthContext";
import { ErrorAlert } from "../../components/ErrorAlert";
import { formatDateTimeRu } from "../../format";
import { userMayReturnTicketByRules } from "../../ticketReturn";

export function TicketDetailsPage() {
  const { id } = useParams();
  const ticketId = Number(id);
  const navigate = useNavigate();
  const { profile } = useAuth();
  const qc = useQueryClient();
  const [confirmReturn, setConfirmReturn] = useState(false);

  const q = useQuery({
    queryKey: ["tickets", "byId", ticketId],
    queryFn: () => getTicketById(ticketId),
    enabled: Number.isFinite(ticketId) && ticketId > 0,
  });

  const returnMut = useMutation({
    mutationFn: () => deleteTicket(ticketId),
    onSuccess: async () => {
      setConfirmReturn(false);
      await qc.invalidateQueries({ queryKey: ["tickets"] });
      navigate(profile?.role === "ADMIN" ? "/tickets" : "/cabinet?tab=tickets");
    },
  });

  const title = q.data?.eventName ?? "Билет";
  const isAdmin = profile?.role === "ADMIN";
  const isOwner = profile != null && q.data != null && profile.userId === q.data.userId;
  const canManageReturn = profile != null && q.data != null && (isAdmin || isOwner);
  const mayReturn = canManageReturn && (isAdmin || userMayReturnTicketByRules(q.data));

  const backTo = profile?.role === "ADMIN" ? "/tickets" : "/cabinet?tab=tickets";

  return (
    <Stack spacing={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="center">
        <Typography variant="h4">{title}</Typography>
        <Button component={RouterLink} to={backTo} variant="outlined">
          Назад
        </Button>
      </Stack>

      <ErrorAlert error={q.error} />
      <ErrorAlert error={returnMut.error} />
      {!q.data && q.isLoading && <Alert severity="info">Загрузка…</Alert>}

      {q.data && (
        <Paper sx={{ p: 2 }}>
          <Stack spacing={1}>
            <Typography fontWeight={600}>{q.data.eventName}</Typography>
            <Typography color="text.secondary">Гость: {q.data.userEmail}</Typography>
            <Typography>Номер билета: {q.data.barcode}</Typography>
            <Typography>Куплен: {formatDateTimeRu(q.data.purchaseDate)}</Typography>
            <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap sx={{ mt: 1 }}>
              <Button component={RouterLink} to={`/events/${q.data.eventId}`} variant="contained" sx={{ alignSelf: "flex-start" }}>
                Страница мероприятия
              </Button>
              {canManageReturn && (
                <Button
                  variant="outlined"
                  color="warning"
                  disabled={!mayReturn || returnMut.isPending}
                  onClick={() => setConfirmReturn(true)}
                  sx={{ alignSelf: "flex-start" }}
                >
                  Вернуть билет
                </Button>
              )}
            </Stack>
            {canManageReturn && !mayReturn && (
              <Typography variant="caption" color="text.secondary">
                Возврат недоступен: мероприятие уже началось или завершено.
              </Typography>
            )}
          </Stack>
        </Paper>
      )}

      <Dialog open={confirmReturn} onClose={() => !returnMut.isPending && setConfirmReturn(false)}>
        <DialogTitle>Вернуть билет?</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary">
            Билет будет аннулирован. Это действие нельзя отменить.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmReturn(false)} disabled={returnMut.isPending}>
            Отмена
          </Button>
          <Button color="warning" variant="contained" disabled={returnMut.isPending} onClick={() => returnMut.mutate()}>
            Вернуть
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
}
