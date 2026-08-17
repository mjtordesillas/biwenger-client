import { matchDayFromRound } from './round.js'
import { teamCrestUrl } from './image-cdn.js'
import { toAsRows } from './as-score.js'
import { toSofaScoreRows } from './sofascore-score.js'
import { mediaPoints } from './media-score.js'
import { toSubstitutionRows } from './substitutions.js'

const AS_FORMAT = '1'
const SOFASCORE_FORMAT = '2'

const toTeamView = (team) => ({
  id: team.id,
  name: team.name,
  score: team.score,
  crestUrl: teamCrestUrl(team.id),
})

const toAsView = (report) => ({
  points: report.points?.[AS_FORMAT] ?? null,
  rows: toAsRows(report.rawStats),
})

const toSofaScoreView = (report) => ({
  points: report.points?.[SOFASCORE_FORMAT] ?? null,
  rows: toSofaScoreRows(report.rawStats),
})

export const toMatchDayDetailsView = (reports, { matchDay }) => {
  const report = reports.filter(Boolean).find((candidate) => matchDayFromRound(candidate.match.round) === matchDay)
  if (!report) {
    return null
  }
  const as = toAsView(report)
  const sofaScore = toSofaScoreView(report)
  return {
    matchDay,
    kickoff: report.match.date,
    home: toTeamView(report.match.home),
    away: toTeamView(report.match.away),
    as,
    sofaScore,
    media: mediaPoints({ as: as.points, sofaScore: sofaScore.points }),
    substitutions: toSubstitutionRows(report.events),
  }
}
