import { toPlayerView } from './player-view.js'

// Shapes a {formation, players} pair (see biwenger-client.js's
// getLineup) into what the lineup screen needs: the formation string
// as-is (e.g. "3-5-2" — a client groups by each player's own `position`
// field, same as it already does for position codes elsewhere, rather
// than parsing this string), and the starting eleven via the plain
// player view.
export const toLineupView = ({ formation, players }) => ({
  formation,
  players: players.map(toPlayerView),
})
