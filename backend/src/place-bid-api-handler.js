import { createBiwengerClient } from './biwenger-client.js'

// Private write proxy for POST /market/my-bids/{playerId}. Upstream
// details are deliberately collapsed so a response can never disclose
// credentials. Covers both a malformed request body and an upstream
// failure alike, same as save-lineup-api-handler.js.
export const createPlaceBidApiHandler = (dependencies = {}) => {
  const {
    biwengerClient = createBiwengerClient(),
    credentials = { email: process.env.BIWENGER_EMAIL, password: process.env.BIWENGER_PASSWORD },
  } = dependencies

  return async (event) => {
    try {
      const playerId = Number(event.pathParameters?.playerId)
      if (!Number.isSafeInteger(playerId) || playerId <= 0) throw new Error('invalid player id')
      const { amount } = JSON.parse(event.body)
      if (!Number.isSafeInteger(amount) || amount <= 0) throw new Error('invalid amount')
      await biwengerClient.placeBid({ ...credentials, playerId, amount })
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
