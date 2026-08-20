import { toPlayerView } from './player-view.js'

// Shapes a {formation, players} pair (see biwenger-client.js's
// getLineup) into what the lineup screen needs: the formation string
// as-is (a client slices `players` by the formation's counts, in
// order — see docs/biwenger-api-notes.md § "Starting lineup" for why
// that's the only reliable grouping), and the starting eleven via the
// plain player view. A `null` entry (a vacant slot, see getLineup) is
// kept as `null` rather than shaped — it carries no player to view,
// and dropping it here would shift every later slot's band, same as
// getLineup already avoids.
export const toLineupView = ({ formation, players }) => ({
  formation,
  players: players.map((player) => (player ? toPlayerView(player) : null)),
})
