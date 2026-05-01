import React from "react";
import { Link as RouterLink, Outlet, useNavigate } from "react-router-dom";
import {
  AppBar,
  Avatar,
  Box,
  Button,
  Container,
  Menu,
  MenuItem,
  Toolbar,
  Typography,
} from "@mui/material";
import LoginIcon from "@mui/icons-material/Login";
import PersonAddIcon from "@mui/icons-material/PersonAdd";
import ConfirmationNumberOutlinedIcon from "@mui/icons-material/ConfirmationNumberOutlined";
import EventAvailableOutlinedIcon from "@mui/icons-material/EventAvailableOutlined";
import SettingsOutlinedIcon from "@mui/icons-material/SettingsOutlined";
import LogoutOutlinedIcon from "@mui/icons-material/LogoutOutlined";
import KeyboardArrowDownIcon from "@mui/icons-material/KeyboardArrowDown";
import { useAuth } from "../auth/AuthContext";

export function AppLayout() {
  const navigate = useNavigate();
  const { profile, logout, ready } = useAuth();
  const [menuAnchor, setMenuAnchor] = React.useState<null | HTMLElement>(null);
  const menuOpen = Boolean(menuAnchor);

  const initial = profile?.fullName?.trim()?.charAt(0) ?? profile?.email?.charAt(0) ?? "?";

  const closeMenu = () => setMenuAnchor(null);

  return (
    <Box sx={{ minHeight: "100dvh" }}>
      <AppBar
        position="fixed"
        elevation={0}
        sx={{
          backdropFilter: "blur(10px)",
          backgroundColor: "rgba(255,255,255,0.86)",
          borderBottom: "1px solid rgba(15,23,42,0.08)",
          color: "text.primary",
        }}
      >
        <Toolbar>
          <Typography
            component={RouterLink}
            to="/"
            variant="h6"
            noWrap
            sx={{
              textDecoration: "none",
              color: "inherit",
              fontWeight: 800,
              letterSpacing: -0.2,
              mr: 2,
            }}
          >
            Event System
          </Typography>

          <Box sx={{ flex: 1 }} />

          <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
            {!ready ? null : profile ? (
              <>
                <Button
                  id="account-menu-button"
                  onClick={(e) => setMenuAnchor(e.currentTarget)}
                  color="inherit"
                  endIcon={<KeyboardArrowDownIcon />}
                  sx={{
                    textTransform: "none",
                    borderRadius: 2,
                    px: 1,
                    py: 0.5,
                    maxWidth: { xs: 160, sm: 280 },
                  }}
                  aria-controls={menuOpen ? "account-menu" : undefined}
                  aria-haspopup="true"
                  aria-expanded={menuOpen ? "true" : undefined}
                >
                  <Avatar sx={{ width: 32, height: 32, bgcolor: "primary.main", mr: 1 }}>
                    {initial.toUpperCase()}
                  </Avatar>
                  <Typography variant="body2" noWrap sx={{ display: { xs: "none", sm: "block" }, fontWeight: 600 }}>
                    {profile.fullName?.trim() || profile.email}
                  </Typography>
                </Button>
                <Menu
                  id="account-menu"
                  anchorEl={menuAnchor}
                  open={menuOpen}
                  onClose={closeMenu}
                  anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
                  transformOrigin={{ vertical: "top", horizontal: "right" }}
                  slotProps={{ paper: { sx: { minWidth: 220, mt: 1, borderRadius: 2 } } }}
                >
                  {(profile.role === "USER" || profile.role === "ADMIN") && (
                    <MenuItem
                      onClick={() => {
                        closeMenu();
                        navigate("/cabinet?tab=tickets");
                      }}
                    >
                      <ConfirmationNumberOutlinedIcon fontSize="small" sx={{ mr: 1.5, opacity: 0.75 }} />
                      Мои билеты
                    </MenuItem>
                  )}
                  {profile.role === "ORGANIZER" && (
                    <MenuItem
                      onClick={() => {
                        closeMenu();
                        navigate("/cabinet?tab=events");
                      }}
                    >
                      <EventAvailableOutlinedIcon fontSize="small" sx={{ mr: 1.5, opacity: 0.75 }} />
                      Мои мероприятия
                    </MenuItem>
                  )}
                  <MenuItem
                    onClick={() => {
                      closeMenu();
                      navigate("/cabinet?tab=profile");
                    }}
                  >
                    <SettingsOutlinedIcon fontSize="small" sx={{ mr: 1.5, opacity: 0.75 }} />
                    Настройки профиля
                  </MenuItem>
                  <MenuItem
                    onClick={() => {
                      closeMenu();
                      logout();
                      navigate("/");
                    }}
                  >
                    <LogoutOutlinedIcon fontSize="small" sx={{ mr: 1.5, opacity: 0.75 }} />
                    Выйти
                  </MenuItem>
                </Menu>
              </>
            ) : (
              <>
                <Button component={RouterLink} to="/login" size="small" startIcon={<LoginIcon />} variant="outlined">
                  Вход
                </Button>
                <Button
                  component={RouterLink}
                  to="/register"
                  size="small"
                  startIcon={<PersonAddIcon />}
                  variant="contained"
                >
                  Регистрация
                </Button>
              </>
            )}
          </Box>
        </Toolbar>
      </AppBar>

      <Box component="main" sx={{ p: { xs: 2, sm: 3 } }}>
        <Toolbar />
        <Container maxWidth="lg" sx={{ pb: 6 }}>
          <Outlet />
        </Container>
      </Box>
    </Box>
  );
}
