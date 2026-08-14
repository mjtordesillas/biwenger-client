// Shapes the raw `prices` array from the player detail endpoint (see
// docs/biwenger-api-notes.md — "Historical market value") into what
// clients actually need: entries for the current season only, including
// preseason. Biwenger returns a trailing ~1 year window with no season
// concept of its own — the season-start rule below is ours, not theirs.

// Our season starts July 1. Computed from `today` rather than a
// hardcoded year, so the cutoff moves forward on its own every July
// without a code change — self-correcting.
const seasonStart = (today) => {
  const year = today.getUTCMonth() >= 6 ? today.getUTCFullYear() : today.getUTCFullYear() - 1
  return Date.UTC(year, 6, 1)
}

// Biwenger dates are `[YY]MMDD` numbers, e.g. 250814 -> 2025-08-14.
const parseYYMMDD = (yymmdd) => {
  const digits = String(yymmdd).padStart(6, '0')
  const year = 2000 + Number(digits.slice(0, 2))
  const month = Number(digits.slice(2, 4)) - 1
  const day = Number(digits.slice(4, 6))
  return Date.UTC(year, month, day)
}

const toIsoDate = (timestamp) => new Date(timestamp).toISOString().slice(0, 10)

export const toPriceHistoryView = (prices, dependencies = {}) => {
  const { today = new Date() } = dependencies
  const cutoff = seasonStart(today)
  return prices
    .filter(([yymmdd]) => parseYYMMDD(yymmdd) >= cutoff)
    .map(([yymmdd, price]) => ({ date: toIsoDate(parseYYMMDD(yymmdd)), price }))
}
