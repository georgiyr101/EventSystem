import { apiDelete, apiGet, apiPost, apiPut } from "./http";
import type { OrganizerRequestDto, OrganizerResponseDto } from "./types";

export function listOrganizers() {
  return apiGet<OrganizerResponseDto[]>("/organizers");
}

export function getOrganizerById(id: number) {
  return apiGet<OrganizerResponseDto>(`/organizers/${id}`);
}

export function searchOrganizersByName(name: string) {
  return apiGet<OrganizerResponseDto[]>("/organizers/search", { name });
}

export function createOrganizer(dto: OrganizerRequestDto) {
  return apiPost<OrganizerResponseDto>("/organizers", dto);
}

export function updateOrganizer(id: number, dto: OrganizerRequestDto) {
  return apiPut<OrganizerResponseDto>(`/organizers/${id}`, dto);
}

export function deleteOrganizer(id: number) {
  return apiDelete(`/organizers/${id}`);
}

