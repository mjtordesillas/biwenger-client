import { createBiwengerClient } from './biwenger-client.js'
import { renderSquadPage } from './render-squad-page.js'

// See docs/coding-conventions/handler-factory-pattern.md — the factory
// injects dependencies (with production defaults), the returned function is
// what Serverless Framework binds to the Lambda.
export const createSquadHandler = (dependencies = {}) => {
  const {
    biwengerClient = createBiwengerClient(),
    credentials = { email: process.env.BIWENGER_EMAIL, password: process.env.BIWENGER_PASSWORD },
  } = dependencies

  return async () => {
    try {
      const players = await biwengerClient.getMySquad(credentials)
      return {
        statusCode: 200,
        headers: { 'Content-Type': 'text/html; charset=utf-8' },
        body: renderSquadPage({ players }),
      }
    } catch {
      // Never leak upstream error details — they could echo back
      // credentials/token fragments. See security-review.
      return {
        statusCode: 502,
        headers: { 'Content-Type': 'text/plain; charset=utf-8' },
        body: 'Could not load your squad from Biwenger right now.',
      }
    }
  }
}
