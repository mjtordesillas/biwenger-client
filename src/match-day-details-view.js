import { matchDayFromRound } from './round.js'
import { teamCrestUrl } from './image-cdn.js'
import { picasBase } from './as-score.js'

const SCORE_FORMAT = '5'
const AS_FORMAT = '1'

const toTeamView = (team) => ({
  id: team.id,
  name: team.name,
  score: team.score,
  crestUrl: teamCrestUrl(team.id),
})

const toAsView = (report) => ({
  points: report.points?.[AS_FORMAT] ?? null,
  rows: [{ type: 'picas', count: report.rawStats.picas, points: picasBase(report.rawStats.picas) }],
})

export const toMatchDayDetailsView = (reports, { matchDay }) => {
  const report = reports.filter(Boolean).find((candidate) => matchDayFromRound(candidate.match.round) === matchDay)
  if (!report) {
    return null
  }
  return {
    matchDay,
    kickoff: report.match.date,
    points: report.points?.[SCORE_FORMAT] ?? null,
    home: toTeamView(report.match.home),
    away: toTeamView(report.match.away),
    as: toAsView(report),
  }
}
