import { createBiwengerClient } from './biwenger-client.js'
import { toPlayerOfferView } from './player-offer-view.js'

// Protection is enforced by API Gateway itself (native API key + usage
// plan — private: true in serverless.yml), same as market-api-handler.js.
export const createPlayerOffersApiHandler = (dependencies = {}) => {
  const {
    biwengerClient = createBiwengerClient(),
    credentials = { email: process.env.BIWENGER_EMAIL, password: process.env.BIWENGER_PASSWORD },
  } = dependencies

  return async () => {
    try {
      const offers = await biwengerClient.getOffersOnMyPlayers(credentials)
      return {
        statusCode: 200,
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        body: JSON.stringify({ players: offers.map(toPlayerOfferView) }),
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
