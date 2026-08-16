import { goalBonus } from './goal-bonus.js'

const SOFASCORE_BASE = [
  [9.5, 14],
  [9.0, 13],
  [8.6, 12],
  [8.2, 11],
  [8.0, 10],
  [7.8, 9],
  [7.6, 8],
  [7.4, 7],
  [7.2, 6],
  [7.0, 5],
  [6.8, 4],
  [6.6, 3],
  [6.4, 2],
  [6.2, 1],
  [6.0, 0],
  [5.8, -1],
  [5.6, -2],
  [5.4, -3],
  [5.2, -4],
  [5.0, -5],
]
const SOFASCORE_BASE_FLOOR = -6
const PENALTY_GOAL_BONUS = 3
const ASSIST_BONUS = 1

export const sofascoreBase = (rating) => {
  const band = SOFASCORE_BASE.find(([threshold]) => rating >= threshold)
  return band ? band[1] : SOFASCORE_BASE_FLOOR
}

export const toSofaScoreRows = (rawStats) => {
  const rows = [{ type: 'sofascore', rating: rawStats.sofascore, points: sofascoreBase(rawStats.sofascore) }]
  if (rawStats.goals) {
    rows.push({ type: 'goal', count: rawStats.goals, points: rawStats.goals * goalBonus(rawStats) })
  }
  if (rawStats.goalsPenalty) {
    rows.push({ type: 'penalty', count: rawStats.goalsPenalty, points: rawStats.goalsPenalty * PENALTY_GOAL_BONUS })
  }
  if (rawStats.assists) {
    rows.push({ type: 'assist', count: rawStats.assists, points: rawStats.assists * ASSIST_BONUS })
  }
  return rows
}
