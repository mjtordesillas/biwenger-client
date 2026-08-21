import { createBiwengerClient } from './biwenger-client.js'
import { toPlayerBidView } from './player-bid-view.js'

// Protection is enforced by API Gateway itself (native API key + usage
// plan — private: true in serverless.yml), same as market-api-handler.js.
export const createPlayerBidsApiHandler = (dependencies = {}) => {
  const {
    biwengerClient = createBiwengerClient(),
    credentials = { email: process.env.BIWENGER_EMAIL, password: process.env.BIWENGER_PASSWORD },
  } = dependencies

  return async () => {
    try {
      const bids = await biwengerClient.getMyBidsOnOtherPlayers(credentials)
      return {
        statusCode: 200,
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        body: JSON.stringify({ players: bids.map(toPlayerBidView) }),
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
