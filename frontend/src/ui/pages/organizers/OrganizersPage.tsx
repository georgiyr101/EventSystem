import React from "react";
import { Box, Button, Divider, Paper, Stack, TextField, Typography } from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link as RouterLink, useSearchParams } from "react-router-dom";
import { createOrganizer, deleteOrganizer, listOrganizers, searchOrganizersByName, updateOrganizer } from "../../../api/organizers";
import type { OrganizerRequestDto, OrganizerResponseDto } from "../../../api/types";
import { ErrorAlert } from "../../components/ErrorAlert";
import { OrganizerFormDialog } from "./OrganizerFormDialog";

export function OrganizersPage() {
  const qc = useQueryClient();
  const [sp, setSp] = useSearchParams();
  const name = sp.get("name") ?? "";

  const organizersQuery = useQuery({
    queryKey: ["organizers", { name }],
    queryFn: () => (name ? searchOrganizersByName(name) : listOrganizers()),
  });

  const createMut = useMutation({
    mutationFn: createOrganizer,
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["organizers"] });
      setCreateOpen(false);
    },
  });

  const updateMut = useMutation({
    mutationFn: ({ id, dto }: { id: number; dto: OrganizerRequestDto }) => updateOrganizer(id, dto),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["organizers"] });
      setEdit(null);
    },
  });

  const deleteMut = useMutation({
    mutationFn: deleteOrganizer,
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["organizers"] });
    },
  });

  const [createOpen, setCreateOpen] = React.useState(false);
  const [edit, setEdit] = React.useState<{ id: number; dto: OrganizerRequestDto } | null>(null);

  const rows: OrganizerResponseDto[] = organizersQuery.data ?? [];

  const setNameParam = (v: string) => {
    const next = new URLSearchParams(sp);
    if (!v) next.delete("name");
    else next.set("name", v);
    setSp(next, { replace: true });
  };

  return (
    <Stack spacing={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="center">
        <Typography variant="h4">Organizers</Typography>
        <Button variant="contained" onClick={() => setCreateOpen(true)}>
          Create
        </Button>
      </Stack>

      <Paper sx={{ p: 2 }}>
        <Stack spacing={2}>
          <Typography variant="subtitle1">Search by name</Typography>
          <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
            <TextField
              label="name"
              value={name}
              onChange={(e) => setNameParam(e.target.value)}
              fullWidth
              helperText="Uses /organizers/search when set"
            />
            <Button variant="outlined" onClick={() => setNameParam("")}>
              Reset
            </Button>
          </Stack>
        </Stack>
      </Paper>

      <ErrorAlert error={organizersQuery.error || createMut.error || updateMut.error || deleteMut.error} />

      <Paper sx={{ p: 2 }}>
        <Stack spacing={1}>
          <Typography variant="subtitle1">Список</Typography>
          <Divider />
          <Stack spacing={1}>
            {rows.map((o) => (
              <Paper key={o.id} variant="outlined" sx={{ p: 1.5 }}>
                <Stack direction={{ xs: "column", sm: "row" }} spacing={2} alignItems={{ sm: "center" }}>
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="h6">
                      {o.name}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Contact: {o.contactInfo ?? "—"}
                    </Typography>
                  </Box>
                  <Stack direction="row" spacing={1}>
                    <Button component={RouterLink} to={`/organizers/${o.id}`} variant="outlined">
                      Open
                    </Button>
                    <Button
                      variant="outlined"
                      onClick={() =>
                        setEdit({ id: o.id, dto: { name: o.name, contactInfo: o.contactInfo ?? "" } })
                      }
                    >
                      Edit
                    </Button>
                    <Button
                      color="error"
                      variant="outlined"
                      onClick={() => deleteMut.mutate(o.id)}
                      disabled={deleteMut.isPending}
                    >
                      Delete
                    </Button>
                  </Stack>
                </Stack>
              </Paper>
            ))}
            {rows.length === 0 && <Typography color="text.secondary">No organizers found.</Typography>}
          </Stack>
        </Stack>
      </Paper>

      <OrganizerFormDialog
        open={createOpen}
        title="Create organizer"
        onClose={() => setCreateOpen(false)}
        onSubmit={(dto) => createMut.mutate(dto)}
        busy={createMut.isPending}
        submitLabel="Create"
      />

      <OrganizerFormDialog
        open={!!edit}
        title="Edit organizer"
        initial={edit?.dto}
        onClose={() => setEdit(null)}
        onSubmit={(dto) => edit && updateMut.mutate({ id: edit.id, dto })}
        busy={updateMut.isPending}
        submitLabel="Save"
      />
    </Stack>
  );
}

