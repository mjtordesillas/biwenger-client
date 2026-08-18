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

  // Keeps `owner` (not just `id`) — squad-player-view.js needs
  // `owner.lockedUntil` for the market-transfer-lock countdown.
  const getSquadEntries = async ({ token, leagueId, userId }) => {
    const response = await httpFetch(`${baseUrl}/user?fields=players(id,owner)`, {
      headers: {
        Authorization: `Bearer ${token}`,
        'X-League': String(leagueId),
        'X-User': String(userId),
        Accept: 'application/json',
      },
    })
    const { data } = await response.json()
    return data.players
  }

  // Shared by getMySquad (checks a player's own sale/offers against it)
  // and getCurrentMarket (filters the requester's own sales out of it) —
  // see docs/biwenger-api-notes.md § "League transfer market" and
  // "Squad player status".
  const getMarketData = async ({ token, leagueId, userId }) => {
    const response = await httpFetch(`${baseUrl}/market`, {
      headers: {
        Authorization: `Bearer ${token}`,
        'X-League': String(leagueId),
        'X-User': String(userId),
        Accept: 'application/json',
      },
    })
    const { data } = await response.json()
    return data
  }

  const getCatalogue = async () => {
    const response = await httpFetch(`${baseUrl}/competitions/la-liga/data?lang=es&score=5`, {
      headers: { Accept: 'application/json' },
    })
    const { data } = await response.json()
    return data.players
  }

  // Biwenger's own price-history entries are keyed by a YYMMDD number
  // (see getPlayerPrices/docs/biwenger-api-notes.md), not an ISO date —
  // needed to look up a draft-owned player's value on their signing date.
  const yymmddFromUnixSeconds = (unixSeconds) => {
    const date = new Date(unixSeconds * 1000)
    const yy = String(date.getUTCFullYear() % 100).padStart(2, '0')
    const mm = String(date.getUTCMonth() + 1).padStart(2, '0')
    const dd = String(date.getUTCDate()).padStart(2, '0')
    return Number(`${yy}${mm}${dd}`)
  }

  // Log in, resolve league/user, fetch the owned players (with their
  // `owner` data), join against the catalogue for name/position/price/
  // status, and cross-reference the market for "is this one of mine
  // that's currently listed" / "what's the standing offer on it, if any"
  // — see docs/biwenger-api-notes.md § "Squad player status".
  // Returns {player, owner, inMarket, offerAmount, draftedPrice} tuples
  // rather than a merged object, same reasoning as getCurrentMarket's
  // {sale, player} — squad-player-view.js does the shaping.
  const getMySquad = async ({ email, password }) => {
    const token = await login({ email, password })
    const { leagueId, userId } = await getAccount({ token })
    const [squadEntries, catalogue, { sales, offers }] = await Promise.all([
      getSquadEntries({ token, leagueId, userId }),
      getCatalogue(),
      getMarketData({ token, leagueId, userId }),
    ])

    // Draft-owned players (owner.price absent — never bought) don't
    // carry their market value at draft time anywhere in the squad or
    // market responses; the only place it exists is each player's own
    // price history, keyed by day.
    const draftedEntries = squadEntries.filter(({ owner }) => owner.price == null)
    const draftedPriceById = new Map(
      await Promise.all(
        draftedEntries.map(async ({ id, owner }) => {
          const prices = await getPlayerPrices({ playerId: id })
          const entry = prices.find(([yymmdd]) => yymmdd === yymmddFromUnixSeconds(owner.date))
          return [id, entry?.[1] ?? null]
        })
      )
    )

    return squadEntries
      .map(({ id, owner }) => {
        const player = catalogue[String(id)]
        if (!player) return null
        const inMarket = sales.some((sale) => sale.user?.id === userId && sale.player.id === id)
        // First matching offer's amount — a player hasn't been observed
        // with more than one standing offer at once; not disambiguated
        // further without a concrete case that needs it.
        const offer = offers.find((offer) => offer.to?.id === userId && offer.requestedPlayers?.includes(id))
        return {
          player,
          owner,
          inMarket,
          offerAmount: offer?.amount ?? null,
          draftedPrice: draftedPriceById.get(id) ?? null,
        }
      })
      .filter(Boolean)
  }

  // League transfer market — see docs/biwenger-api-notes.md. Returns
  // {sale, player} pairs rather than a merged object: `sale.price` (the
  // asking price, what a bid actually costs) and `player.price` (the
  // catalogue's live market value, tracked separately by
  // `priceIncrement`) are two different numbers a market view needs
  // side by side, not one overriding the other — see
  // market-listing-view.js for how they're shaped into the response.
  // Excludes the requester's own listings, same as market.go's IsMyPlayer.
  const getCurrentMarket = async ({ email, password }) => {
    const token = await login({ email, password })
    const { leagueId, userId } = await getAccount({ token })
    const [{ sales }, catalogue] = await Promise.all([
      getMarketData({ token, leagueId, userId }),
      getCatalogue(),
    ])
    return sales
      .filter((sale) => sale.user?.id !== userId)
      .map((sale) => {
        const player = catalogue[String(sale.player.id)]
        return player && { sale, player }
      })
      .filter(Boolean)
  }

  const getPlayerPrices = async ({ playerId }) => {
    const response = await httpFetch(`${baseUrl}/players/la-liga/${playerId}?fields=id,name,prices`, {
      headers: { Accept: 'application/json' },
    })
    const { data } = await response.json()
    return data.prices
  }

  const getSeasonIds = async ({ playerId }) => {
    const response = await httpFetch(`${baseUrl}/players/la-liga/${playerId}?fields=id,name,seasons`, {
      headers: { Accept: 'application/json' },
    })
    const { data } = await response.json()
    return data.seasons
      .filter((season) => !season.competition)
      .map((season) => Number(season.id))
      .sort((a, b) => b - a)
  }

  const getPlayerGameweekPoints = async ({ playerId, season = 'current' }) => {
    const [currentSeasonId, previousSeasonId] = await getSeasonIds({ playerId })
    const seasonId = season === 'previous' ? previousSeasonId : currentSeasonId
    const response = await httpFetch(`${baseUrl}/players/la-liga/${playerId}?fields=id,name,reports&season=${seasonId}`, {
      headers: { Accept: 'application/json' },
    })
    const { data } = await response.json()
    return data.reports
  }

  return { getMySquad, getCurrentMarket, getPlayerPrices, getPlayerGameweekPoints }
}
