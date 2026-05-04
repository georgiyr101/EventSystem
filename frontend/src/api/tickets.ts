import { apiDelete, apiGet, apiPost, apiPut } from "./http";
import type { TicketRequestDto, TicketResponseDto } from "./types";

export type BulkTicketItem = { eventId: number; barcode: string };

export type BulkTicketRequest = {
  userId: number;
  tickets: BulkTicketItem[];
};

export function listTickets(params?: { userId?: number; barcode?: string }) {
  return apiGet<TicketResponseDto[]>("/tickets", params);
}

export function getTicketById(id: number) {
  return apiGet<TicketResponseDto>(`/tickets/${id}`);
}

export function createTicket(dto: TicketRequestDto) {
  return apiPost<TicketResponseDto>("/tickets", dto);
}

export function buyTicketsBulk(body: BulkTicketRequest, transactional = true) {
  return apiPost<TicketResponseDto[]>("/tickets/bulk", body, { transactional });
}

export function updateTicket(id: number, dto: TicketRequestDto) {
  return apiPut<TicketResponseDto>(`/tickets/${id}`, dto);
}

export function deleteTicket(id: number) {
  return apiDelete(`/tickets/${id}`);
}

