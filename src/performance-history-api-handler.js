import { createBiwengerClient } from './biwenger-client.js'
import { toPerformanceHistoryView } from './performance-history-view.js'

export const createPerformanceHistoryApiHandler = (dependencies = {}) => {
  const { biwengerClient = createBiwengerClient() } = dependencies

  return async (event) => {
    const playerId = event?.pathParameters?.playerId
    const season = event?.queryStringParameters?.season === 'previous' ? 'previous' : 'current'
    try {
      const reports = await biwengerClient.getPlayerGameweekPoints({ playerId, season })
      return {
        statusCode: 200,
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        body: JSON.stringify(toPerformanceHistoryView(reports)),
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
