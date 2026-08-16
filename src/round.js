// `round.short` (e.g. "R8") is the only place a report's match day number
// lives — shared by performance-history-view.js (every match day in a
// season) and match-day-details-view.js (one specific match day), so it's
// modeled once rather than reimplemented per view.
export const matchDayFromRound = (round) => Number(String(round.short).replace(/\D/g, ''))
