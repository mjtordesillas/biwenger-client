import { createBiwengerClient } from './biwenger-client.js'
import { toMatchDayDetailsView } from './match-day-details-view.js'

export const createMatchDayDetailsApiHandler = (dependencies = {}) => {
  const { biwengerClient = createBiwengerClient() } = dependencies

  return async (event) => {
    const playerId = event?.pathParameters?.playerId
    const matchDay = Number(event?.queryStringParameters?.matchDay)
    const season = event?.queryStringParameters?.season === 'previous' ? 'previous' : 'current'
    try {
      const reports = await biwengerClient.getPlayerGameweekPoints({ playerId, season })
      const view = toMatchDayDetailsView(reports, { matchDay })
      if (!view) {
        return {
          statusCode: 404,
          headers: { 'Content-Type': 'application/json; charset=utf-8' },
          body: JSON.stringify({ error: 'match_day_not_found' }),
        }
      }
      return {
        statusCode: 200,
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        body: JSON.stringify(view),
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
