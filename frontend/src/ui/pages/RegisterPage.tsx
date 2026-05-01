import { useState, type FormEvent } from "react";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { Box, Button, Paper, Stack, TextField, Typography } from "@mui/material";
import ConfirmationNumberOutlinedIcon from "@mui/icons-material/ConfirmationNumberOutlined";
import EventAvailableOutlinedIcon from "@mui/icons-material/EventAvailableOutlined";
import type { RegisterRequestDto } from "../../api/types";
import { authRegister } from "../../api/auth";
import { useAuth } from "../../auth/AuthContext";
import { ErrorAlert } from "../components/ErrorAlert";

type RoleChoice = RegisterRequestDto["role"];

const roleCards: {
  role: RoleChoice;
  title: string;
  subtitle: string;
  Icon: typeof ConfirmationNumberOutlinedIcon;
}[] = [
  {
    role: "USER",
    title: "Я хочу посещать мероприятия",
    subtitle: "Покупка билетов и личный кабинет с вашими билетами.",
    Icon: ConfirmationNumberOutlinedIcon,
  },
  {
    role: "ORGANIZER",
    title: "Я хочу создавать мероприятия",
    subtitle: "Кабинет организатора и управление своими событиями.",
    Icon: EventAvailableOutlinedIcon,
  },
];

export function RegisterPage() {
  const { setSession } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [fullName, setFullName] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState<RoleChoice>("USER");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<unknown>(null);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      const dto = await authRegister({
        email: email.trim(),
        fullName: fullName.trim(),
        password,
        role,
      });
      setSession(dto);
      navigate("/", { replace: true });
    } catch (err) {
      setError(err);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Stack spacing={2} sx={{ maxWidth: 560, mx: "auto" }}>
      <Typography variant="h4">Регистрация</Typography>
      <Typography color="text.secondary">
        Выберите, как вы планируете пользоваться сервисом. Для организаторов автоматически создаётся профиль в
        каталоге.
      </Typography>
      <ErrorAlert error={error} />
      <Paper sx={{ p: 3, border: "1px solid rgba(15,23,42,0.08)" }} component="form" onSubmit={onSubmit}>
        <Stack spacing={2.5}>
          <Box>
            <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 1.5, fontWeight: 600 }}>
              Как вы хотите использовать Event System?
            </Typography>
            <Stack direction={{ xs: "column", sm: "row" }} spacing={1.5}>
              {roleCards.map(({ role: r, title, subtitle, Icon }) => {
                const selected = role === r;
                return (
                  <Paper
                    key={r}
                    elevation={0}
                    onClick={() => setRole(r)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter" || e.key === " ") {
                        e.preventDefault();
                        setRole(r);
                      }
                    }}
                    role="radio"
                    aria-checked={selected}
                    tabIndex={0}
                    sx={{
                      flex: 1,
                      p: 2,
                      cursor: "pointer",
                      borderRadius: 2,
                      border: "2px solid",
                      borderColor: selected ? "primary.main" : "rgba(15,23,42,0.10)",
                      bgcolor: selected ? "rgba(27,110,243,0.06)" : "background.paper",
                      boxShadow: selected ? "0 0 0 1px rgba(27,110,243,0.35)" : "none",
                      transition: "border-color 0.2s ease, background-color 0.2s ease, box-shadow 0.2s ease",
                      "&:hover": {
                        borderColor: selected ? "primary.main" : "rgba(27,110,243,0.35)",
                        bgcolor: selected ? "rgba(27,110,243,0.08)" : "rgba(15,23,42,0.02)",
                      },
                    }}
                  >
                    <Stack direction="row" spacing={1.5} alignItems="flex-start">
                      <Box
                        sx={{
                          width: 48,
                          height: 48,
                          borderRadius: 2,
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                          bgcolor: selected ? "primary.main" : "rgba(15,23,42,0.06)",
                          color: selected ? "primary.contrastText" : "text.secondary",
                          flexShrink: 0,
                          transition: "background-color 0.2s ease, color 0.2s ease",
                        }}
                      >
                        <Icon sx={{ fontSize: 26 }} />
                      </Box>
                      <Box sx={{ minWidth: 0 }}>
                        <Typography variant="subtitle1" sx={{ fontWeight: 700, lineHeight: 1.3, mb: 0.5 }}>
                          {title}
                        </Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ lineHeight: 1.45 }}>
                          {subtitle}
                        </Typography>
                      </Box>
                    </Stack>
                  </Paper>
                );
              })}
            </Stack>
          </Box>

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
            label="Полное имя"
            autoComplete="name"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            required
            fullWidth
          />
          <TextField
            label="Пароль (мин. 8 символов)"
            type="password"
            autoComplete="new-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            inputProps={{ minLength: 8 }}
            fullWidth
          />
          <Button type="submit" variant="contained" size="large" disabled={busy}>
            {busy ? "Создание…" : "Создать аккаунт"}
          </Button>
          <Box>
            <Typography variant="body2" color="text.secondary">
              Уже есть аккаунт?{" "}
              <RouterLink to="/login" style={{ fontWeight: 600 }}>
                Войти
              </RouterLink>
            </Typography>
          </Box>
        </Stack>
      </Paper>
    </Stack>
  );
}
