/** Цена в белорусских рублях (отображение для пользователя). */
export function formatPriceBr(value: number): string {
  return `${value} Br`;
}

/** Дата и время в привычном виде (например, 15 мая 2026 г., 18:00). */
export function formatDateTimeRu(iso: string | null | undefined): string {
  if (!iso) return "—";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return String(iso);
  return d.toLocaleString("ru-BY", {
    day: "numeric",
    month: "long",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

const EVENT_STATUS_FALLBACK: Record<string, string> = {
  PLANNED: "Запланировано",
  ONGOING: "Идёт сейчас",
  COMPLETED: "Завершено",
  CANCELLED: "Отменено",
  SOLD_OUT: "Билеты проданы",
};

/** Подпись статуса события для пользователя (человеческий текст, не код). */
export function eventStatusLabel(e: { statusDescription?: string; statusCode?: string }): string {
  const d = e.statusDescription?.trim();
  if (d) return d;
  const code = e.statusCode?.trim();
  if (code && EVENT_STATUS_FALLBACK[code]) return EVENT_STATUS_FALLBACK[code];
  return code || "—";
}
