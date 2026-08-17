import { createBiwengerClient } from './biwenger-client.js'
import { toMarketListingView } from './market-listing-view.js'

// Protection is enforced by API Gateway itself (native API key + usage
// plan — private: true in serverless.yml), same as squad-api-handler.js.
export const createMarketApiHandler = (dependencies = {}) => {
  const {
    biwengerClient = createBiwengerClient(),
    credentials = { email: process.env.BIWENGER_EMAIL, password: process.env.BIWENGER_PASSWORD },
  } = dependencies

  return async () => {
    try {
      const listings = await biwengerClient.getCurrentMarket(credentials)
      return {
        statusCode: 200,
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        body: JSON.stringify({ players: listings.map(toMarketListingView) }),
      }
    } catch {
      // Never leak upstream error details — they could echo back
      // credentials/token fragments.
      return {
        statusCode: 502,
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        body: JSON.stringify({ error: 'upstream_error' }),
      }
    }
  }
}
