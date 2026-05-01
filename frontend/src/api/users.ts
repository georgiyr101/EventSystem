import { apiDelete, apiGet, apiPost, apiPut } from "./http";
import type { UserRequestDto, UserResponseDto } from "./types";

export function listUsers() {
  return apiGet<UserResponseDto[]>("/users");
}

export function getUserById(id: number) {
  return apiGet<UserResponseDto>(`/users/${id}`);
}

export function findUserByEmail(email: string) {
  return apiGet<UserResponseDto>("/users/find", { email });
}

export function registerUser(dto: UserRequestDto) {
  return apiPost<UserResponseDto>("/users/register", dto);
}

export function updateUser(id: number, dto: UserRequestDto) {
  return apiPut<UserResponseDto>(`/users/${id}`, dto);
}

export function deleteUser(id: number) {
  return apiDelete(`/users/${id}`);
}

