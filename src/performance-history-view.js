const SCORE_FORMAT = '5'

const matchDayFromRound = (round) => Number(String(round.short).replace(/\D/g, ''))

export const toPerformanceHistoryView = (reports) => {
  const gameweeks = reports
    .filter(Boolean)
    .map((report) => ({
      matchDay: matchDayFromRound(report.match.round),
      points: report.points?.[SCORE_FORMAT] ?? null,
    }))
    .sort((a, b) => a.matchDay - b.matchDay)
  return { gameweeks }
}
