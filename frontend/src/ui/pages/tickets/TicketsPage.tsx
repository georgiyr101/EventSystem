import React from "react";
import { Box, Button, Divider, Paper, Stack, TextField, Typography } from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link as RouterLink, useSearchParams } from "react-router-dom";
import { createTicket, deleteTicket, listTickets, updateTicket } from "../../../api/tickets";
import type { TicketRequestDto, TicketResponseDto } from "../../../api/types";
import { ErrorAlert } from "../../components/ErrorAlert";
import { formatDateTimeRu } from "../../format";
import { TicketFormDialog } from "./TicketFormDialog";

export function TicketsPage() {
  const qc = useQueryClient();
  const [sp, setSp] = useSearchParams();
  const barcode = sp.get("barcode") ?? "";

  const ticketsQuery = useQuery({
    queryKey: ["tickets", { barcode }],
    queryFn: () =>
      listTickets({
        barcode: barcode || undefined,
      }),
  });

  const createMut = useMutation({
    mutationFn: createTicket,
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["tickets"] });
      setCreateOpen(false);
    },
  });

  const updateMut = useMutation({
    mutationFn: ({ id, dto }: { id: number; dto: TicketRequestDto }) => updateTicket(id, dto),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["tickets"] });
      setEdit(null);
    },
  });

  const deleteMut = useMutation({
    mutationFn: deleteTicket,
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["tickets"] });
    },
  });

  const [createOpen, setCreateOpen] = React.useState(false);
  const [edit, setEdit] = React.useState<{ id: number; dto: TicketRequestDto } | null>(null);

  const rows: TicketResponseDto[] = ticketsQuery.data ?? [];

  const setParam = (k: string, v: string) => {
    const next = new URLSearchParams(sp);
    if (!v) next.delete(k);
    else next.set(k, v);
    setSp(next, { replace: true });
  };

  return (
    <Stack spacing={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="center">
        <Typography variant="h4">Билеты</Typography>
        <Button variant="contained" onClick={() => setCreateOpen(true)}>
          Добавить билет
        </Button>
      </Stack>

      <Paper sx={{ p: 2 }}>
        <Stack spacing={2}>
          <Typography variant="subtitle1">Поиск по штрихкоду</Typography>
          <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
            <TextField
              label="Штрихкод"
              value={barcode}
              onChange={(e) => setParam("barcode", e.target.value)}
              fullWidth
            />
            <Button
              variant="outlined"
              onClick={() => {
                setParam("barcode", "");
              }}
            >
              Сбросить
            </Button>
          </Stack>
        </Stack>
      </Paper>

      <ErrorAlert error={ticketsQuery.error || createMut.error || updateMut.error || deleteMut.error} />

      <Paper sx={{ p: 2 }}>
        <Stack spacing={1}>
          <Typography variant="subtitle1">Список</Typography>
          <Divider />
          <Stack spacing={1}>
            {rows.map((t) => (
              <Paper key={t.id} variant="outlined" sx={{ p: 1.5 }}>
                <Stack direction={{ xs: "column", sm: "row" }} spacing={2} alignItems={{ sm: "center" }}>
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="h6">{t.eventName}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      Гость: {t.userEmail}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Номер билета: {t.barcode} · {formatDateTimeRu(t.purchaseDate)}
                    </Typography>
                  </Box>
                  <Stack direction="row" spacing={1}>
                    <Button component={RouterLink} to={`/tickets/${t.id}`} variant="outlined">
                      Подробнее
                    </Button>
                    <Button
                      variant="outlined"
                      onClick={() =>
                        setEdit({
                          id: t.id,
                          dto: { eventId: t.eventId, userId: t.userId, barcode: t.barcode },
                        })
                      }
                    >
                      Изменить
                    </Button>
                    <Button
                      color="error"
                      variant="outlined"
                      onClick={() => deleteMut.mutate(t.id)}
                      disabled={deleteMut.isPending}
                    >
                      Удалить
                    </Button>
                  </Stack>
                </Stack>
              </Paper>
            ))}
            {rows.length === 0 && <Typography color="text.secondary">Билеты не найдены.</Typography>}
          </Stack>
        </Stack>
      </Paper>

      <TicketFormDialog
        open={createOpen}
        title="Новый билет"
        onClose={() => setCreateOpen(false)}
        onSubmit={(dto) => createMut.mutate(dto)}
        busy={createMut.isPending}
        submitLabel="Создать"
      />
      <TicketFormDialog
        open={!!edit}
        title="Редактирование билета"
        initial={edit?.dto}
        onClose={() => setEdit(null)}
        onSubmit={(dto) => edit && updateMut.mutate({ id: edit.id, dto })}
        busy={updateMut.isPending}
        submitLabel="Сохранить"
      />
    </Stack>
  );
}
