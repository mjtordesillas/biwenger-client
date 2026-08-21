import { toPlayerView } from './player-view.js'

// Shapes a {offer, player} pair (see biwenger-client.js's
// getOffersOnMyPlayers) into what clients need for a standing offer: the
// player view, plus the offer amount and, where identifiable, who made
// it. Unlike market-listing-view.js there's no separate asking price to
// track alongside the catalogue value — toPlayerView's `price` already
// is the one number that matters here, next to `amount`.
export const toPlayerOfferView = ({ offer, player }) => ({
  ...toPlayerView(player),
  amount: offer.amount,
  bidder: offer.from?.name ?? null,
})
