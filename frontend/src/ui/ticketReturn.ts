import type { TicketResponseDto } from "../api/types";

/** Whether a non-admin may return this ticket per business rules (server enforces the same). */
export function userMayReturnTicketByRules(t: TicketResponseDto): boolean {
  const st = t.eventStatusCode;
  if (st === "COMPLETED" || st === "ONGOING") return false;
  if (t.eventStartDate) {
    const start = new Date(t.eventStartDate).getTime();
    if (!Number.isNaN(start) && start <= Date.now()) return false;
  }
  return true;
}
