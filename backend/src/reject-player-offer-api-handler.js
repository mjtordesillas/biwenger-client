import { createBiwengerClient } from './biwenger-client.js'

// Private write proxy for PUT /market/offers/{offerId}. Upstream details are
// deliberately collapsed so a response can never disclose credentials.
export const createRejectPlayerOfferApiHandler = (dependencies = {}) => {
  const {
    biwengerClient = createBiwengerClient(),
    credentials = { email: process.env.BIWENGER_EMAIL, password: process.env.BIWENGER_PASSWORD },
  } = dependencies

  return async (event) => {
    try {
      const offerId = Number(event.pathParameters?.offerId)
      if (!Number.isSafeInteger(offerId) || offerId <= 0) throw new Error('invalid offer id')
      await biwengerClient.rejectOffer({ ...credentials, offerId })
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
