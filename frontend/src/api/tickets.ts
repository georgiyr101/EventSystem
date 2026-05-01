import { apiDelete, apiGet, apiPost, apiPut } from "./http";
import type { TicketRequestDto, TicketResponseDto } from "./types";

export function listTickets(params?: { userId?: number; barcode?: string }) {
  return apiGet<TicketResponseDto[]>("/tickets", params);
}

export function getTicketById(id: number) {
  return apiGet<TicketResponseDto>(`/tickets/${id}`);
}

export function createTicket(dto: TicketRequestDto) {
  return apiPost<TicketResponseDto>("/tickets", dto);
}

export function updateTicket(id: number, dto: TicketRequestDto) {
  return apiPut<TicketResponseDto>(`/tickets/${id}`, dto);
}

export function deleteTicket(id: number) {
  return apiDelete(`/tickets/${id}`);
}

