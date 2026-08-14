import { createBiwengerClient } from './biwenger-client.js'
import { toPriceHistoryView } from './price-history-view.js'

export const createPriceHistoryApiHandler = (dependencies = {}) => {
  const { biwengerClient = createBiwengerClient() } = dependencies

  return async (event) => {
    const playerId = event?.pathParameters?.playerId
    try {
      const prices = await biwengerClient.getPlayerPrices({ playerId })
      return {
        statusCode: 200,
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        body: JSON.stringify(toPriceHistoryView(prices)),
      }
    } catch {
      return {
        statusCode: 502,
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        body: JSON.stringify({ error: 'upstream_error' }),
      }
    }
  }
}
