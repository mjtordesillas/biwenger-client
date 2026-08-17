import { createSquadApiHandler } from './squad-api-handler.js'
import { createMarketApiHandler } from './market-api-handler.js'
import { createPriceHistoryApiHandler } from './price-history-api-handler.js'
import { createPerformanceHistoryApiHandler } from './performance-history-api-handler.js'
import { createMatchDayDetailsApiHandler } from './match-day-details-api-handler.js'

// Production wiring lives here, separate from handler logic — see
// docs/coding-conventions/handler-factory-pattern.md.
export const squad = createSquadApiHandler()
export const market = createMarketApiHandler()
export const priceHistory = createPriceHistoryApiHandler()
export const performanceHistory = createPerformanceHistoryApiHandler()
export const matchDayDetails = createMatchDayDetailsApiHandler()
