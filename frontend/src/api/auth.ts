import { apiGet, apiPostPublic, apiPut } from "./http";
import type { AuthResponseDto, LoginRequestDto, ProfileUpdateRequestDto, RegisterRequestDto } from "./types";

export function authLogin(body: LoginRequestDto) {
  return apiPostPublic<AuthResponseDto>("/auth/login", body);
}

export function authRegister(body: RegisterRequestDto) {
  return apiPostPublic<AuthResponseDto>("/auth/register", body);
}

export function authMe() {
  return apiGet<AuthResponseDto>("/auth/me");
}

export function authUpdateProfile(body: ProfileUpdateRequestDto) {
  return apiPut<AuthResponseDto>("/auth/profile", body);
}
