import { createBiwengerClient } from './biwenger-client.js'
import { toPriceHistoryView } from './price-history-view.js'

// Protection is enforced by API Gateway itself (native API key + usage
// plan — private: true in serverless.yml), same as the squad endpoint.
// See docs/adrs/002-native-api-gateway-key-for-squad-endpoint.md. Unlike
// squad, no Biwenger credentials are needed — the upstream endpoint is
// public — but ours still sits behind our own gateway key.
export const createPriceHistoryApiHandler = (dependencies = {}) => {
  const { biwengerClient = createBiwengerClient() } = dependencies

  return async (event) => {
    const playerId = event?.pathParameters?.playerId
    try {
      const prices = await biwengerClient.getPlayerPrices({ playerId })
      return {
        statusCode: 200,
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        body: JSON.stringify({ prices: toPriceHistoryView(prices) }),
      }
    } catch {
      // Never leak upstream error details.
      return {
        statusCode: 502,
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        body: JSON.stringify({ error: 'upstream_error' }),
      }
    }
  }
}
