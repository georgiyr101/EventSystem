import type { ErrorResponse } from "./types";
import { getAccessToken } from "../auth/token";

export class ApiError extends Error {
  status: number;
  path?: string;

  constructor(message: string, status: number, path?: string) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.path = path;
  }
}

const API_PREFIX = "/api/v1";

function buildUrl(path: string, query?: Record<string, unknown>) {
  const url = new URL(API_PREFIX + path, window.location.origin);
  if (query) {
    for (const [k, v] of Object.entries(query)) {
      if (v === undefined || v === null || v === "") continue;
      url.searchParams.set(k, String(v));
    }
  }
  return url.toString();
}

function jsonHeaders(): HeadersInit {
  const headers: Record<string, string> = {
    Accept: "application/json",
    "Content-Type": "application/json",
  };
  const token = getAccessToken();
  if (token) headers.Authorization = `Bearer ${token}`;
  return headers;
}

function readHeaders(): HeadersInit {
  const headers: Record<string, string> = { Accept: "application/json" };
  const token = getAccessToken();
  if (token) headers.Authorization = `Bearer ${token}`;
  return headers;
}

async function parseError(res: Response): Promise<ApiError> {
  try {
    const body = (await res.json()) as Partial<ErrorResponse>;
    return new ApiError(body.message ?? res.statusText, res.status, body.path);
  } catch {
    return new ApiError(res.statusText, res.status);
  }
}

export async function apiGet<T>(path: string, query?: Record<string, unknown>): Promise<T> {
  const res = await fetch(buildUrl(path, query), {
    method: "GET",
    headers: readHeaders(),
  });
  if (!res.ok) throw await parseError(res);
  return (await res.json()) as T;
}

export async function apiPost<T>(path: string, body?: unknown, query?: Record<string, unknown>): Promise<T> {
  const res = await fetch(buildUrl(path, query), {
    method: "POST",
    headers: jsonHeaders(),
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (!res.ok) throw await parseError(res);
  return (await res.json()) as T;
}

/** POST without Bearer (e.g. login/register). */
export async function apiPostPublic<T>(path: string, body?: unknown): Promise<T> {
  const res = await fetch(buildUrl(path), {
    method: "POST",
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (!res.ok) throw await parseError(res);
  return (await res.json()) as T;
}

export async function apiPut<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(buildUrl(path), {
    method: "PUT",
    headers: jsonHeaders(),
    body: JSON.stringify(body),
  });
  if (!res.ok) throw await parseError(res);
  return (await res.json()) as T;
}

export async function apiPatch<T>(path: string, query?: Record<string, unknown>): Promise<T> {
  const res = await fetch(buildUrl(path, query), {
    method: "PATCH",
    headers: readHeaders(),
  });
  if (!res.ok) throw await parseError(res);
  return (await res.json()) as T;
}

export async function apiDelete(path: string): Promise<void> {
  const res = await fetch(buildUrl(path), {
    method: "DELETE",
    headers: readHeaders(),
  });
  if (!res.ok) throw await parseError(res);
}

