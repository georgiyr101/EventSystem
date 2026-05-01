import { Button, Stack, Typography } from "@mui/material";
import { Link as RouterLink } from "react-router-dom";

export function NotFoundPage() {
  return (
    <Stack spacing={2}>
      <Typography variant="h4">Страница не найдена</Typography>
      <Typography color="text.secondary">Проверьте адрес или вернитесь на главную.</Typography>
      <Button component={RouterLink} to="/" variant="contained">
        На главную
      </Button>
    </Stack>
  );
}

