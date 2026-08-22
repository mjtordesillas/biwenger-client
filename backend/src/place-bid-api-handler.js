import { createBiwengerClient } from './biwenger-client.js'

// Private write proxy for POST /market/my-bids. Both playerId and
// amount travel in the body — not a path segment like the other
// player-id-keyed writes (list/unlist) — because API Gateway rejects
// two sibling resources under the same parent with differently-named
// path variables: DELETE /market/my-bids/{offerId} (remove-bid) already
// claims that slot, so POST can't also put a variable (named playerId)
// there. Upstream details are deliberately collapsed so a response can
// never disclose credentials. Covers both a malformed request body and
// an upstream failure alike, same as save-lineup-api-handler.js.
export const createPlaceBidApiHandler = (dependencies = {}) => {
  const {
    biwengerClient = createBiwengerClient(),
    credentials = { email: process.env.BIWENGER_EMAIL, password: process.env.BIWENGER_PASSWORD },
  } = dependencies

  return async (event) => {
    try {
      const { playerId, amount } = JSON.parse(event.body)
      if (!Number.isSafeInteger(playerId) || playerId <= 0) throw new Error('invalid player id')
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
