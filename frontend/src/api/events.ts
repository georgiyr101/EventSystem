import { apiDelete, apiGet, apiPatch, apiPost, apiPut } from "./http";
import type { EventRequestDto, EventResponseDto, EventSearchParams, Page } from "./types";

export function listEvents() {
  return apiGet<EventResponseDto[]>("/events");
}

export function getEventById(id: number) {
  return apiGet<EventResponseDto>(`/events/${id}`);
}

export function createEvent(dto: EventRequestDto) {
  return apiPost<EventResponseDto>("/events", dto);
}

export function updateEvent(id: number, dto: EventRequestDto) {
  return apiPut<EventResponseDto>(`/events/${id}`, dto);
}

export function deleteEvent(id: number) {
  return apiDelete(`/events/${id}`);
}

export function changeEventStatus(id: number, newStatus: string) {
  return apiPatch<EventResponseDto>(`/events/${id}/status`, { newStatus });
}

export function filterEventsByStatus(status: string) {
  return apiGet<EventResponseDto[]>("/events/filter", { status });
}

export function searchEvents(params: EventSearchParams) {
  const { page, size, sort, ...rest } = params;
  return apiGet<Page<EventResponseDto>>("/events/search", {
    ...rest,
    page,
    size,
    sort,
  });
}

export function getEventSoldTicketsCount(eventId: number) {
  return apiGet<{ soldCount: number }>(`/events/${eventId}/sold-tickets-count`);
}

