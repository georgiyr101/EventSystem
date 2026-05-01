import React from "react";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { Alert, Box, Button, Paper, Stack, TextField, Typography } from "@mui/material";
import { authLogin } from "../../api/auth";
import { ApiError } from "../../api/http";
import { useAuth } from "../../auth/AuthContext";
import { ErrorAlert } from "../components/ErrorAlert";

export function LoginPage() {
  const { setSession } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = React.useState("");
  const [password, setPassword] = React.useState("");
  const [busy, setBusy] = React.useState(false);
  const [error, setError] = React.useState<unknown>(null);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      const dto = await authLogin({ email: email.trim(), password });
      setSession(dto);
      navigate("/", { replace: true });
    } catch (err) {
      setError(err);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Stack spacing={2} sx={{ maxWidth: 480, mx: "auto" }}>
      <Typography variant="h4">Вход</Typography>
      <Typography color="text.secondary">
        Войдите, чтобы покупать билеты или управлять событиями организатора.
      </Typography>
      <ErrorAlert error={error} />
      {error instanceof ApiError && error.status === 401 && (
        <Alert severity="warning">Проверьте email и пароль.</Alert>
      )}
      <Paper sx={{ p: 3, border: "1px solid rgba(15,23,42,0.08)" }} component="form" onSubmit={onSubmit}>
        <Stack spacing={2}>
          <TextField
            label="Email"
            type="email"
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            fullWidth
          />
          <TextField
            label="Пароль"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            fullWidth
          />
          <Button type="submit" variant="contained" size="large" disabled={busy}>
            {busy ? "Вход…" : "Войти"}
          </Button>
          <Box>
            <Typography variant="body2" color="text.secondary">
              Нет аккаунта?{" "}
              <RouterLink to="/register" style={{ fontWeight: 600 }}>
                Регистрация
              </RouterLink>
            </Typography>
          </Box>
        </Stack>
      </Paper>
    </Stack>
  );
}
