import { Alert } from "@mui/material";

export function LoadingAlert({ show }: { show: boolean }) {
  if (!show) return null;
  return <Alert severity="info">Загрузка…</Alert>;
}

