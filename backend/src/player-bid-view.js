import { toPlayerView } from './player-view.js'

// Shapes a {offer, sale, player} triple (see biwenger-client.js's
// getMyBidsOnOtherPlayers) into what clients need for one of my own
// outgoing bids: the player view with `price` overridden to the asking
// price (what the bid is actually being judged against — same
// reasoning as market-listing-view.js) and `marketValue` added for the
// catalogue's own value, plus `seller` (the owner, or null for a
// free-agent listing — same as a market listing's seller), `until`
// (the bid's own expiry), `amount` (my bid), and `offerId` (the offer's
// own id — needed by remove-a-bid, same field player-offer-view.js
// already exposes for the incoming-offer side; not otherwise part of
// toPlayerView's shape).
export const toPlayerBidView = ({ offer, sale, player }) => ({
  offerId: offer.id,
  ...toPlayerView(player),
  price: sale.price,
  marketValue: player.price,
  until: offer.until,
  seller: sale.user?.name ?? null,
  amount: offer.amount,
})
