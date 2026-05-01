import React from "react";
import { Box, CircularProgress } from "@mui/material";
import { Navigate } from "react-router-dom";
import type { AppRole } from "../../api/types";
import { useAuth } from "../../auth/AuthContext";

type Props = {
  children: React.ReactNode;
  roles?: AppRole[];
};

export function RequireAuth({ children, roles }: Props) {
  const { profile, ready } = useAuth();

  if (!ready) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", py: 6 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!profile) {
    return <Navigate to="/login" replace />;
  }

  if (roles && !roles.includes(profile.role)) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
}
