import { createBiwengerClient, DEFAULT_LISTING_PRICE } from './biwenger-client.js'

// The fixed asking price (no price entry in the UI) is applied
// server-side, not client-supplied, so the write's shape stays fixed
// regardless of the caller — see DEFAULT_LISTING_PRICE in
// biwenger-client.js, shared with cycleListings.

// Private write proxy for POST /market/my-listings/{playerId}. Upstream
// details are deliberately collapsed so a response can never disclose
// credentials.
export const createListPlayerApiHandler = (dependencies = {}) => {
  const {
    biwengerClient = createBiwengerClient(),
    credentials = { email: process.env.BIWENGER_EMAIL, password: process.env.BIWENGER_PASSWORD },
  } = dependencies

  return async (event) => {
    try {
      const playerId = Number(event.pathParameters?.playerId)
      if (!Number.isSafeInteger(playerId) || playerId <= 0) throw new Error('invalid player id')
      await biwengerClient.listPlayer({ ...credentials, playerId, price: DEFAULT_LISTING_PRICE })
      return {
        statusCode: 200,
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        body: JSON.stringify({}),
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
