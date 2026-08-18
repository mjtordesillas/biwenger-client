import { toPlayerView } from './player-view.js'

// Shapes a {player, owner, inMarket, offerAmount} tuple (see
// biwenger-client.js's getMySquad) into what a squad player card needs
// beyond the plain player view — see docs/biwenger-api-notes.md § "Squad
// player status" for where each of these comes from:
// - `signedAt`: unix seconds for when this ownership started — a
//   purchase date for a market buy, the draft date for a draft-owned
//   player. Always present.
// - `signedPrice`: what was paid, or `null` — draft-owned players were
//   never bought, so `owner.price` doesn't apply to them.
// - `lockedUntil`: unix seconds for when Biwenger's post-purchase
//   transfer lock lifts, or `null` if the player is already sellable
//   (draft-owned, or the lock has passed).
// - `inMarket`: whether *I've* currently listed this player.
// - `offerAmount`: the amount of a standing offer on this player, or
//   `null` if there isn't one — the raw number, not just a boolean,
//   since a client needs it to judge the offer against `price` (this
//   view's market value).
// - `status`: raw catalogue fitness status ("ok", "injured", "doubt",
//   "sanctioned", "unknown", "discarded").
export const toSquadPlayerView = ({ player, owner, inMarket, offerAmount }) => ({
  ...toPlayerView(player),
  signedAt: owner.date,
  signedPrice: owner.price ?? null,
  lockedUntil: owner.lockedUntil ?? null,
  inMarket,
  offerAmount,
  status: player.status ?? 'unknown',
})
