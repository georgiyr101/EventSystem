import React from "react";
import { Box, Button, Divider, Paper, Stack, TextField, Typography } from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link as RouterLink, useSearchParams } from "react-router-dom";
import { deleteUser, findUserByEmail, listUsers, registerUser, updateUser } from "../../../api/users";
import type { UserRequestDto, UserResponseDto } from "../../../api/types";
import { ErrorAlert } from "../../components/ErrorAlert";
import { UserFormDialog } from "./UserFormDialog";

export function UsersPage() {
  const qc = useQueryClient();
  const [sp, setSp] = useSearchParams();
  const email = sp.get("email") ?? "";

  const usersQuery = useQuery({
    queryKey: ["users", { email }],
    queryFn: async () => {
      if (email) return [await findUserByEmail(email)];
      return listUsers();
    },
  });

  const createMut = useMutation({
    mutationFn: registerUser,
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["users"] });
      setCreateOpen(false);
    },
  });

  const updateMut = useMutation({
    mutationFn: ({ id, dto }: { id: number; dto: UserRequestDto }) => updateUser(id, dto),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["users"] });
      setEdit(null);
    },
  });

  const deleteMut = useMutation({
    mutationFn: deleteUser,
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["users"] });
    },
  });

  const [createOpen, setCreateOpen] = React.useState(false);
  const [edit, setEdit] = React.useState<{ id: number; dto: UserRequestDto } | null>(null);

  const rows: UserResponseDto[] = usersQuery.data ?? [];

  const setEmailParam = (v: string) => {
    const next = new URLSearchParams(sp);
    if (!v) next.delete("email");
    else next.set("email", v);
    setSp(next, { replace: true });
  };

  return (
    <Stack spacing={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="center">
        <Typography variant="h4">Users</Typography>
        <Button variant="contained" onClick={() => setCreateOpen(true)}>
          Register
        </Button>
      </Stack>

      <Paper sx={{ p: 2 }}>
        <Stack spacing={2}>
          <Typography variant="subtitle1">Find by email</Typography>
          <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
            <TextField
              label="email"
              value={email}
              onChange={(e) => setEmailParam(e.target.value)}
              fullWidth
              helperText="Uses /users/find when set"
            />
            <Button variant="outlined" onClick={() => setEmailParam("")}>
              Reset
            </Button>
          </Stack>
        </Stack>
      </Paper>

      <ErrorAlert error={usersQuery.error || createMut.error || updateMut.error || deleteMut.error} />

      <Paper sx={{ p: 2 }}>
        <Stack spacing={1}>
          <Typography variant="subtitle1">Список</Typography>
          <Divider />
          <Stack spacing={1}>
            {rows.map((u) => (
              <Paper key={u.id} variant="outlined" sx={{ p: 1.5 }}>
                <Stack direction={{ xs: "column", sm: "row" }} spacing={2} alignItems={{ sm: "center" }}>
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="h6">
                      {u.fullName ?? "—"}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Email: {u.email}
                    </Typography>
                  </Box>
                  <Stack direction="row" spacing={1}>
                    <Button component={RouterLink} to={`/users/${u.id}`} variant="outlined">
                      Open
                    </Button>
                    <Button
                      variant="outlined"
                      onClick={() => setEdit({ id: u.id, dto: { fullName: u.fullName ?? "", email: u.email } })}
                    >
                      Edit
                    </Button>
                    <Button
                      color="error"
                      variant="outlined"
                      onClick={() => deleteMut.mutate(u.id)}
                      disabled={deleteMut.isPending}
                    >
                      Delete
                    </Button>
                  </Stack>
                </Stack>
              </Paper>
            ))}
            {rows.length === 0 && <Typography color="text.secondary">No users found.</Typography>}
          </Stack>
        </Stack>
      </Paper>

      <UserFormDialog
        open={createOpen}
        title="Register user"
        onClose={() => setCreateOpen(false)}
        onSubmit={(dto) => createMut.mutate(dto)}
        busy={createMut.isPending}
        submitLabel="Register"
      />

      <UserFormDialog
        open={!!edit}
        title="Edit user"
        initial={edit?.dto}
        onClose={() => setEdit(null)}
        onSubmit={(dto) => edit && updateMut.mutate({ id: edit.id, dto })}
        busy={updateMut.isPending}
        submitLabel="Save"
      />
    </Stack>
  );
}

