import { createBiwengerClient } from './biwenger-client.js'
import { toLineupView } from './lineup-view.js'

// Write side of lineup-api-handler.js, for swap-lineup-players — see
// docs/biwenger-api-notes.md § "Starting lineup — write". Protection is
// enforced by API Gateway itself (native API key + usage plan — private:
// true in serverless.yml), same as the other handlers.
export const createSaveLineupApiHandler = (dependencies = {}) => {
  const {
    biwengerClient = createBiwengerClient(),
    credentials = { email: process.env.BIWENGER_EMAIL, password: process.env.BIWENGER_PASSWORD },
  } = dependencies

  return async (event) => {
    try {
      const { formation, playerIds } = JSON.parse(event.body)
      const lineup = await biwengerClient.saveLineup({ ...credentials, formation, playerIds })
      return {
        statusCode: 200,
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        body: JSON.stringify(toLineupView(lineup)),
      }
    } catch {
      // Never leak upstream error details — they could echo back
      // credentials/token fragments. Covers both a malformed request
      // body and an upstream failure alike, same as the read handler.
      return {
        statusCode: 502,
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        body: JSON.stringify({ error: 'upstream_error' }),
      }
    }
  }
}
