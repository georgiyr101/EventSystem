import { apiDelete, apiGet, apiPost, apiPut } from "./http";
import type { CategoryRequestDto, CategoryResponseDto } from "./types";

export function listCategories(params?: { name?: string }) {
  return apiGet<CategoryResponseDto[]>("/categories", params);
}

export function getCategoryById(id: number) {
  return apiGet<CategoryResponseDto>(`/categories/${id}`);
}

export function createCategory(dto: CategoryRequestDto) {
  return apiPost<CategoryResponseDto>("/categories", dto);
}

export function updateCategory(id: number, dto: CategoryRequestDto) {
  return apiPut<CategoryResponseDto>(`/categories/${id}`, dto);
}

export function deleteCategory(id: number) {
  return apiDelete(`/categories/${id}`);
}

