import { createBiwengerClient } from './biwenger-client.js'

// Private write proxy for DELETE /market/my-listings/{playerId}. Upstream
// details are deliberately collapsed so a response can never disclose
// credentials.
export const createUnlistPlayerApiHandler = (dependencies = {}) => {
  const {
    biwengerClient = createBiwengerClient(),
    credentials = { email: process.env.BIWENGER_EMAIL, password: process.env.BIWENGER_PASSWORD },
  } = dependencies

  return async (event) => {
    try {
      const playerId = Number(event.pathParameters?.playerId)
      if (!Number.isSafeInteger(playerId) || playerId <= 0) throw new Error('invalid player id')
      await biwengerClient.unlistPlayer({ ...credentials, playerId })
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
