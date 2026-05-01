import { createBrowserRouter, Navigate } from "react-router-dom";
import { AppLayout } from "./ui/AppLayout";
import { HomePage } from "./ui/pages/HomePage";
import { EventsPage } from "./ui/pages/events/EventsPage";
import { EventDetailsPage } from "./ui/pages/events/EventDetailsPage";
import { TicketsPage } from "./ui/pages/tickets/TicketsPage";
import { TicketDetailsPage } from "./ui/pages/tickets/TicketDetailsPage";
import { UsersPage } from "./ui/pages/users/UsersPage";
import { UserDetailsPage } from "./ui/pages/users/UserDetailsPage";
import { OrganizersPage } from "./ui/pages/organizers/OrganizersPage";
import { OrganizerDetailsPage } from "./ui/pages/organizers/OrganizerDetailsPage";
import { CategoriesPage } from "./ui/pages/categories/CategoriesPage";
import { CategoryDetailsPage } from "./ui/pages/categories/CategoryDetailsPage";
import { NotFoundPage } from "./ui/pages/NotFoundPage";
import { LoginPage } from "./ui/pages/LoginPage";
import { RegisterPage } from "./ui/pages/RegisterPage";
import { CabinetPage } from "./ui/pages/cabinet/CabinetPage";
import { RequireAuth } from "./ui/components/RequireAuth";
import { RequireAdmin } from "./ui/components/RequireAdmin";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <AppLayout />,
    children: [
      { index: true, element: <HomePage /> },

      { path: "login", element: <LoginPage /> },
      { path: "register", element: <RegisterPage /> },

      { path: "events", element: <EventsPage /> },
      { path: "events/:id", element: <EventDetailsPage /> },

      { path: "me/tickets", element: <Navigate to="/cabinet?tab=tickets" replace /> },
      { path: "organizer/events", element: <Navigate to="/cabinet?tab=events" replace /> },

      {
        path: "cabinet",
        element: (
          <RequireAuth>
            <CabinetPage />
          </RequireAuth>
        ),
      },

      {
        path: "tickets",
        element: (
          <RequireAdmin>
            <TicketsPage />
          </RequireAdmin>
        ),
      },
      {
        path: "tickets/:id",
        element: (
          <RequireAuth roles={["USER", "ADMIN"]}>
            <TicketDetailsPage />
          </RequireAuth>
        ),
      },

      {
        path: "users",
        element: (
          <RequireAdmin>
            <UsersPage />
          </RequireAdmin>
        ),
      },
      {
        path: "users/:id",
        element: (
          <RequireAdmin>
            <UserDetailsPage />
          </RequireAdmin>
        ),
      },

      {
        path: "organizers",
        element: (
          <RequireAdmin>
            <OrganizersPage />
          </RequireAdmin>
        ),
      },
      {
        path: "organizers/:id",
        element: (
          <RequireAdmin>
            <OrganizerDetailsPage />
          </RequireAdmin>
        ),
      },

      {
        path: "categories",
        element: (
          <RequireAdmin>
            <CategoriesPage />
          </RequireAdmin>
        ),
      },
      {
        path: "categories/:id",
        element: (
          <RequireAdmin>
            <CategoryDetailsPage />
          </RequireAdmin>
        ),
      },

      { path: "404", element: <NotFoundPage /> },
      { path: "*", element: <Navigate to="/404" replace /> },
    ],
  },
]);
