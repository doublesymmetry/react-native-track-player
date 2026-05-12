// iTunes <itunes:duration> can be a raw seconds count ("3600"),
// "MM:SS", or "HH:MM:SS". Returns total seconds, or 0 if unparseable.
export function parseItunesDuration(value: string | undefined): number {
  if (!value) return 0;
  const trimmed = value.trim();
  if (!trimmed) return 0;

  if (!trimmed.includes(':')) {
    const n = Number(trimmed);
    return Number.isFinite(n) && n > 0 ? Math.floor(n) : 0;
  }

  const parts = trimmed.split(':').map((p) => Number(p));
  if (parts.some((n) => !Number.isFinite(n) || n < 0)) return 0;

  let total = 0;
  if (parts.length === 3) {
    const [h, m, s] = parts;
    total = h * 3600 + m * 60 + s;
  } else if (parts.length === 2) {
    const [m, s] = parts;
    total = m * 60 + s;
  } else {
    return 0;
  }
  return Math.floor(total);
}
