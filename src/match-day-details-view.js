import { matchDayFromRound } from './round.js'
import { teamCrestUrl } from './image-cdn.js'

const toTeamView = (team) => ({
  id: team.id,
  name: team.name,
  score: team.score,
  crestUrl: teamCrestUrl(team.id),
})

// Slice 1 of view-match-day-details: header only (which match, when, the
// score) — no points breakdown yet. See
// docs/backlog/to-do/view-match-day-details.md.
export const toMatchDayDetailsView = (reports, { matchDay }) => {
  const report = reports.filter(Boolean).find((candidate) => matchDayFromRound(candidate.match.round) === matchDay)
  if (!report) {
    return null
  }
  return {
    matchDay,
    kickoff: report.match.date,
    home: toTeamView(report.match.home),
    away: toTeamView(report.match.away),
  }
}
