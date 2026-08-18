import { toPlayerView } from './player-view.js'

// Shapes a {player, owner, inMarket, hasOffer} tuple (see
// biwenger-client.js's getMySquad) into what a squad player card needs
// beyond the plain player view — see docs/biwenger-api-notes.md § "Squad
// player status" for where each of these comes from:
// - `lockedUntil`: unix seconds for when Biwenger's post-purchase
//   transfer lock lifts, or `null` if the player is already sellable
//   (draft-owned, or the lock has passed).
// - `inMarket` / `hasOffer`: whether *I've* currently listed this player,
//   and whether someone has a standing offer on it.
// - `status`: raw catalogue fitness status ("ok", "injured", "doubt",
//   "sanctioned", "unknown", "discarded").
export const toSquadPlayerView = ({ player, owner, inMarket, hasOffer }) => ({
  ...toPlayerView(player),
  lockedUntil: owner.lockedUntil ?? null,
  inMarket,
  hasOffer,
  status: player.status ?? 'unknown',
})
