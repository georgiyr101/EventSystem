import { Alert } from "@mui/material";
import type { ApiError } from "../../api/http";

export function ErrorAlert({ error }: { error: unknown }) {
  if (!error) return null;
  const apiError = error as Partial<ApiError>;
  const msg =
    typeof apiError.message === "string" && apiError.message.length > 0
      ? apiError.message
      : "Unexpected error";
  const status = typeof apiError.status === "number" ? ` (HTTP ${apiError.status})` : "";
  return <Alert severity="error">{msg + status}</Alert>;
}

