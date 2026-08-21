import { createSquadApiHandler } from './squad-api-handler.js'
import { createMarketApiHandler } from './market-api-handler.js'
import { createMyMarketListingsApiHandler } from './my-market-listings-api-handler.js'
import { createPlayerOffersApiHandler } from './player-offers-api-handler.js'
import { createRejectPlayerOfferApiHandler } from './reject-player-offer-api-handler.js'
import { createAcceptPlayerOfferApiHandler } from './accept-player-offer-api-handler.js'
import { createUnlistPlayerApiHandler } from './unlist-player-api-handler.js'
import { createPlayerBidsApiHandler } from './player-bids-api-handler.js'
import { createLineupApiHandler } from './lineup-api-handler.js'
import { createSaveLineupApiHandler } from './save-lineup-api-handler.js'
import { createPriceHistoryApiHandler } from './price-history-api-handler.js'
import { createPerformanceHistoryApiHandler } from './performance-history-api-handler.js'
import { createMatchDayDetailsApiHandler } from './match-day-details-api-handler.js'

// Production wiring lives here, separate from handler logic — see
// docs/coding-conventions/handler-factory-pattern.md.
export const squad = createSquadApiHandler()
export const market = createMarketApiHandler()
export const myMarketListings = createMyMarketListingsApiHandler()
export const playerOffers = createPlayerOffersApiHandler()
export const rejectPlayerOffer = createRejectPlayerOfferApiHandler()
export const acceptPlayerOffer = createAcceptPlayerOfferApiHandler()
export const unlistPlayer = createUnlistPlayerApiHandler()
export const playerBids = createPlayerBidsApiHandler()
export const lineup = createLineupApiHandler()
export const saveLineup = createSaveLineupApiHandler()
export const priceHistory = createPriceHistoryApiHandler()
export const performanceHistory = createPerformanceHistoryApiHandler()
export const matchDayDetails = createMatchDayDetailsApiHandler()
