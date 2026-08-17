import { toPlayerView } from './player-view.js'

// Shapes a {sale, player} pair (see biwenger-client.js's getCurrentMarket)
// into what clients need for a market listing: the player view, plus the
// three market-specific facts that don't belong on a plain squad player —
// the asking price, the seller (or null for a free-agent slot), and when
// the listing expires. `price` here is the asking price (what a bid
// costs), distinct from `marketValue` (the catalogue's live value,
// already tracked by toPlayerView's priceIncrement) — see
// docs/biwenger-api-notes.md.
export const toMarketListingView = ({ sale, player }) => ({
  ...toPlayerView(player),
  price: sale.price,
  marketValue: player.price,
  until: sale.until,
  seller: sale.user?.name ?? null,
})
