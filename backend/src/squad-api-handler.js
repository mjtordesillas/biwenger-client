import { createBiwengerClient } from './biwenger-client.js'
import { toSquadPlayerView } from './squad-player-view.js'

// Protection is enforced by API Gateway itself (native API key + usage
// plan — private: true in serverless.yml), not in this handler. See
// docs/adrs/002-native-api-gateway-key-for-squad-endpoint.md. Unauthorized
// requests never reach this Lambda.
export const createSquadApiHandler = (dependencies = {}) => {
  const {
    biwengerClient = createBiwengerClient(),
    credentials = { email: process.env.BIWENGER_EMAIL, password: process.env.BIWENGER_PASSWORD },
  } = dependencies

  return async () => {
    try {
      const players = await biwengerClient.getMySquad(credentials)
      return {
        statusCode: 200,
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        body: JSON.stringify({ players: players.map(toSquadPlayerView) }),
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
