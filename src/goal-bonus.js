const GOAL_BONUS = { 1: 6, 2: 5, 3: 4, 4: 3 }

const positionFromRawStats = (rawStats) => [1, 2, 3, 4].find((position) => rawStats[`pos${position}`])

export const goalBonus = (rawStats) => GOAL_BONUS[positionFromRawStats(rawStats)] ?? null
