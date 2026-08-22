import { createBiwengerClient } from './biwenger-client.js'

// Private write proxy for POST /market/cycle-listings — no path/body
// params, acts on the whole account. Upstream details are deliberately
// collapsed so a response can never disclose credentials.
export const createCycleListingsApiHandler = (dependencies = {}) => {
  const {
    biwengerClient = createBiwengerClient(),
    credentials = { email: process.env.BIWENGER_EMAIL, password: process.env.BIWENGER_PASSWORD },
  } = dependencies

  return async () => {
    try {
      const { unlisted, listed } = await biwengerClient.cycleListings(credentials)
      return {
        statusCode: 200,
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        body: JSON.stringify({ unlisted, listed }),
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
