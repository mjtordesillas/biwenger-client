const BASE_URL = 'https://biwenger.as.com/api/v2'

// Talks to Biwenger's unofficial v2 API. See docs/rat.md and
// docs/adrs/001-unofficial-biwenger-v2-api-over-browser-automation.md for
// why these endpoints/headers, and docs/coding-conventions/factory-functions.md
// for why this is a factory rather than a plain module of exported functions.
export const createBiwengerClient = (dependencies = {}) => {
  const { fetch: httpFetch = fetch, baseUrl = BASE_URL } = dependencies

  const login = async ({ email, password }) => {
    const response = await httpFetch(`${baseUrl}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify({ email, password }),
    })
    const data = await response.json()
    if (!data.token) {
      throw new Error('Biwenger login failed: no token in response')
    }
    return data.token
  }

  const getAccount = async ({ token }) => {
    const response = await httpFetch(`${baseUrl}/account`, {
      headers: { Authorization: `Bearer ${token}`, Accept: 'application/json' },
    })
    const { data } = await response.json()
    const [league] = data.leagues
    return { leagueId: league.id, userId: league.user.id }
  }

  const getSquadPlayerIds = async ({ token, leagueId, userId }) => {
    const response = await httpFetch(`${baseUrl}/user?fields=players(id,owner)`, {
      headers: {
        Authorization: `Bearer ${token}`,
        'X-League': String(leagueId),
        'X-User': String(userId),
        Accept: 'application/json',
      },
    })
    const { data } = await response.json()
    return data.players.map((player) => player.id)
  }

  const getCatalogue = async () => {
    const response = await httpFetch(`${baseUrl}/competitions/la-liga/data?lang=es&score=5`, {
      headers: { Accept: 'application/json' },
    })
    const { data } = await response.json()
    return data.players
  }

  // The one operation Slice 1 needs: log in, resolve league/user, fetch the
  // owned player ids, and join them against the public catalogue for
  // name/position/price. Not split into a reusable client method per call
  // because nothing else needs those calls individually yet.
  const getMySquad = async ({ email, password }) => {
    const token = await login({ email, password })
    const { leagueId, userId } = await getAccount({ token })
    const playerIds = await getSquadPlayerIds({ token, leagueId, userId })
    const catalogue = await getCatalogue()
    return playerIds.map((id) => catalogue[String(id)]).filter(Boolean)
  }

  const getPlayerPrices = async ({ playerId }) => {
    const response = await httpFetch(`${baseUrl}/players/la-liga/${playerId}?fields=id,name,prices`, {
      headers: { Accept: 'application/json' },
    })
    const { data } = await response.json()
    return data.prices
  }

  const getCurrentSeasonId = async ({ playerId }) => {
    const response = await httpFetch(`${baseUrl}/players/la-liga/${playerId}?fields=id,name,seasons`, {
      headers: { Accept: 'application/json' },
    })
    const { data } = await response.json()
    const laLigaSeasonIds = data.seasons.filter((season) => !season.competition).map((season) => Number(season.id))
    return Math.max(...laLigaSeasonIds)
  }

  const getPlayerGameweekPoints = async ({ playerId }) => {
    const season = await getCurrentSeasonId({ playerId })
    const response = await httpFetch(`${baseUrl}/players/la-liga/${playerId}?fields=id,name,reports&season=${season}`, {
      headers: { Accept: 'application/json' },
    })
    const { data } = await response.json()
    return data.reports
  }

  return { getMySquad, getPlayerPrices, getPlayerGameweekPoints }
}
