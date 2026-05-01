import React from "react";
import { Box, Button, Divider, Paper, Stack, TextField, Typography } from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link as RouterLink, useSearchParams } from "react-router-dom";
import { createCategory, deleteCategory, listCategories, updateCategory } from "../../../api/categories";
import type { CategoryRequestDto, CategoryResponseDto } from "../../../api/types";
import { ErrorAlert } from "../../components/ErrorAlert";
import { CategoryFormDialog } from "./CategoryFormDialog";

export function CategoriesPage() {
  const qc = useQueryClient();
  const [sp, setSp] = useSearchParams();
  const name = sp.get("name") ?? "";

  const categoriesQuery = useQuery({
    queryKey: ["categories", { name }],
    queryFn: () => listCategories({ name: name || undefined }),
  });

  const createMut = useMutation({
    mutationFn: createCategory,
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["categories"] });
      setCreateOpen(false);
    },
  });

  const updateMut = useMutation({
    mutationFn: ({ id, dto }: { id: number; dto: CategoryRequestDto }) => updateCategory(id, dto),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["categories"] });
      setEdit(null);
    },
  });

  const deleteMut = useMutation({
    mutationFn: deleteCategory,
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["categories"] });
    },
  });

  const [createOpen, setCreateOpen] = React.useState(false);
  const [edit, setEdit] = React.useState<{ id: number; dto: CategoryRequestDto } | null>(null);

  const rows: CategoryResponseDto[] = categoriesQuery.data ?? [];

  const setNameParam = (v: string) => {
    const next = new URLSearchParams(sp);
    if (!v) next.delete("name");
    else next.set("name", v);
    setSp(next, { replace: true });
  };

  return (
    <Stack spacing={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="center">
        <Typography variant="h4">Categories</Typography>
        <Button variant="contained" onClick={() => setCreateOpen(true)}>
          Create
        </Button>
      </Stack>

      <Paper sx={{ p: 2 }}>
        <Stack spacing={2}>
          <Typography variant="subtitle1">Filter by name</Typography>
          <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
            <TextField
              label="name"
              value={name}
              onChange={(e) => setNameParam(e.target.value)}
              fullWidth
              helperText="Uses /categories?name=..."
            />
            <Button variant="outlined" onClick={() => setNameParam("")}>
              Reset
            </Button>
          </Stack>
        </Stack>
      </Paper>

      <ErrorAlert error={categoriesQuery.error || createMut.error || updateMut.error || deleteMut.error} />

      <Paper sx={{ p: 2 }}>
        <Stack spacing={1}>
          <Typography variant="subtitle1">Список</Typography>
          <Divider />
          <Stack spacing={1}>
            {rows.map((c) => (
              <Paper key={c.id} variant="outlined" sx={{ p: 1.5 }}>
                <Stack direction={{ xs: "column", sm: "row" }} spacing={2} alignItems={{ sm: "center" }}>
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="h6">
                      {c.name}
                    </Typography>
                  </Box>
                  <Stack direction="row" spacing={1}>
                    <Button component={RouterLink} to={`/categories/${c.id}`} variant="outlined">
                      Open
                    </Button>
                    <Button variant="outlined" onClick={() => setEdit({ id: c.id, dto: { name: c.name } })}>
                      Edit
                    </Button>
                    <Button
                      color="error"
                      variant="outlined"
                      onClick={() => deleteMut.mutate(c.id)}
                      disabled={deleteMut.isPending}
                    >
                      Delete
                    </Button>
                  </Stack>
                </Stack>
              </Paper>
            ))}
            {rows.length === 0 && <Typography color="text.secondary">No categories found.</Typography>}
          </Stack>
        </Stack>
      </Paper>

      <CategoryFormDialog
        open={createOpen}
        title="Create category"
        onClose={() => setCreateOpen(false)}
        onSubmit={(dto) => createMut.mutate(dto)}
        busy={createMut.isPending}
        submitLabel="Create"
      />

      <CategoryFormDialog
        open={!!edit}
        title="Edit category"
        initial={edit?.dto}
        onClose={() => setEdit(null)}
        onSubmit={(dto) => edit && updateMut.mutate({ id: edit.id, dto })}
        busy={updateMut.isPending}
        submitLabel="Save"
      />
    </Stack>
  );
}

