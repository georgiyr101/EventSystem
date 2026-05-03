export type Id = number;

export type AppRole = "USER" | "ORGANIZER" | "ADMIN";

export interface AuthResponseDto {
  tokenType: string;
  accessToken: string;
  userId: Id;
  email: string;
  fullName: string;
  role: AppRole;
  organizerId: Id | null;
}

export interface LoginRequestDto {
  email: string;
  password: string;
}

export interface RegisterRequestDto {
  email: string;
  fullName: string;
  password: string;
  role: "USER" | "ORGANIZER";
}

export interface ProfileUpdateRequestDto {
  fullName: string;
  email: string;
  currentPassword?: string;
  newPassword?: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // 0-based page index
  size: number;
}

// ===== Events =====

export type EventStatusCode =
  | "DRAFT"
  | "PUBLISHED"
  | "CANCELLED"
  | "FINISHED"
  | string;

export interface EventResponseDto {
  id: Id;
  name: string;
  startDate: string | null;
  statusDescription: string;
  statusCode: EventStatusCode;
  ticketPrice: number;
  organizerName: string;
  categoryNames: string[];
}

export interface EventRequestDto {
  name: string;
  startDate: string; // LocalDateTime
  endDate: string; // LocalDateTime
  maxParticipants: number;
  ticketPrice: number;
  organizerId: Id;
  categoryIds: Id[];
}

export interface EventSearchParams {
  category?: string;
  minPrice?: number;
  organizer?: string;
  useNative?: boolean;
  page?: number;
  size?: number;
  sort?: string; // e.g. "id,desc"
}

// ===== Tickets =====

export interface TicketResponseDto {
  id: Id;
  eventId: Id;
  eventName: string;
  /** ISO date-time from API when available */
  eventStartDate?: string | null;
  /** Event status: PLANNED, ONGOING, COMPLETED, … */
  eventStatusCode?: string | null;
  userId: Id;
  userEmail: string;
  barcode: string;
  purchaseDate: string | null;
}

export interface TicketRequestDto {
  eventId: Id;
  /** Legacy; ignored for USER — server uses JWT */
  userId?: Id;
  barcode: string;
}

// ===== Users =====

export interface UserResponseDto {
  id: Id;
  fullName: string | null;
  email: string;
}

export interface UserRequestDto {
  fullName: string;
  email: string;
}

// ===== Organizers =====

export interface OrganizerResponseDto {
  id: Id;
  name: string;
  contactInfo: string | null;
}

export interface OrganizerRequestDto {
  name: string;
  contactInfo: string;
}

// ===== Categories =====

export interface CategoryResponseDto {
  id: Id;
  name: string;
}

export interface CategoryRequestDto {
  name: string;
}

// ===== Errors =====

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

