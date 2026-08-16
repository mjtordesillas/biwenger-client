import { goalBonus } from './goal-bonus.js'

const PICAS_BASE = { 0: -2, 1: 2, 2: 6, 3: 10, 4: 14, SC: 0 }
const PENALTY_GOAL_BONUS = 3
const RED_CARD_PENALTY = -6
const SECOND_YELLOW_CARD_PENALTY = -3

export const picasBase = (picas) => PICAS_BASE[picas] ?? null

export const toAsRows = (rawStats) => {
  const rows = [{ type: 'picas', count: rawStats.picas, points: picasBase(rawStats.picas) }]
  if (rawStats.goals) {
    rows.push({ type: 'goal', count: rawStats.goals, points: rawStats.goals * goalBonus(rawStats) })
  }
  if (rawStats.goalsPenalty) {
    rows.push({ type: 'penalty', count: rawStats.goalsPenalty, points: rawStats.goalsPenalty * PENALTY_GOAL_BONUS })
  }
  if (rawStats.redCard) {
    rows.push({ type: 'redCard', count: rawStats.redCard, points: rawStats.redCard * RED_CARD_PENALTY })
  }
  if (rawStats.secondYellowCard) {
    rows.push({ type: 'secondYellowCard', count: rawStats.secondYellowCard, points: rawStats.secondYellowCard * SECOND_YELLOW_CARD_PENALTY })
  }
  return rows
}
