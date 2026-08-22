const BASE_URL = 'https://biwenger.as.com/api/v2'

// Fixed asking price for every listing this app creates — no price
// entry in the UI, shared by listPlayerData (via
// list-player-api-handler.js) and cycleListings, which both need the
// same fixed value.
export const DEFAULT_LISTING_PRICE = 35_000_000

// Same rules as the Android popup's (now-removed) client-side version:
// no-standing-offer candidates first, falling back to with-offer ones
// only to fill remaining slots, capped at maxListings. No real
// listing-history API exists (checked, same RAT as list/unlist), so
// there's no true "least recently listed" ranking — ties just keep
// squad-response order. Exported for direct narrow-unit testing, per
// docs/ways-of-working/testing-strategy.md.
export const selectPlayersToList = (squadTuples, maxListings = 5) => {
  const eligible = squadTuples.filter(({ owner, inMarket }) => !inMarket && owner.lockedUntil == null)
  const withoutOffer = eligible.filter(({ offerAmount }) => offerAmount == null)
  const withOffer = eligible.filter(({ offerAmount }) => offerAmount != null)
  return [...withoutOffer, ...withOffer].slice(0, maxListings).map(({ player }) => player.id)
}

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
    // credits is account-wide (not per-league) — see docs/biwenger-api-notes.md
    // § "Starting lineup — write"'s off-position credit-cost note. Only
    // getLineup/saveLineup destructure it; harmless extra field for
    // getMySquad/getCurrentMarket, which don't.
    return { leagueId: league.id, userId: league.user.id, credits: data.account.credits }
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

  // Rejects a received purchase offer. Verified against the live API on
  // 2026-08-21; see docs/biwenger-api-notes.md § "Incoming offers — write".
  const rejectOfferData = async ({ token, leagueId, userId, offerId }) => {
    const response = await httpFetch(`${baseUrl}/offers/${offerId}`, {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${token}`,
        'X-League': String(leagueId),
        'X-User': String(userId),
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify({ status: 'rejected' }),
    })
    if (!response.ok) throw new Error(`Biwenger reject offer failed: ${response.status}`)
  }

  // Accepts a received purchase offer — same endpoint/shape as
  // rejectOfferData, only the status value differs. NOT verified against
  // the live API yet (see docs/biwenger-api-notes.md § "Incoming offers —
  // write"): accepting is irreversible, so it's built against the
  // verified endpoint's documented shape and left unverified live until
  // there's a real offer worth actually accepting.
  const acceptOfferData = async ({ token, leagueId, userId, offerId }) => {
    const response = await httpFetch(`${baseUrl}/offers/${offerId}`, {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${token}`,
        'X-League': String(leagueId),
        'X-User': String(userId),
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify({ status: 'accepted' }),
    })
    if (!response.ok) throw new Error(`Biwenger accept offer failed: ${response.status}`)
  }

  // Unlists one of the requester's own market listings. Verified against
  // the live API on 2026-08-21 (captured from Biwenger's own web app via
  // browser DevTools, then reproduced here); see docs/biwenger-api-notes.md
  // § "My market listings — write (unlist)". Keyed on the player id via a
  // query param, not a path segment or a separate sale id — a user can
  // only have one active listing per player.
  const unlistPlayerData = async ({ token, leagueId, userId, playerId }) => {
    const response = await httpFetch(`${baseUrl}/market?player=${playerId}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`,
        'X-League': String(leagueId),
        'X-User': String(userId),
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
    })
    if (!response.ok) throw new Error(`Biwenger unlist player failed: ${response.status}`)
  }

  // Lists one of the requester's own squad players on the market.
  // Verified against the live API on 2026-08-21 (captured from
  // Biwenger's own web app via browser DevTools, then reproduced here);
  // see docs/biwenger-api-notes.md § "My market listings — write
  // (list)". The only reference-project hint (pablopb3/biwenger-api's
  // SendPlayersToMarket) turned out wrong on both counts it guessed at
  // — a different `type` value, and a hardcoded price that ignored the
  // parameter it took in — so nothing from it carried over.
  const listPlayerData = async ({ token, leagueId, userId, playerId, price }) => {
    const response = await httpFetch(`${baseUrl}/market`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
        'X-League': String(leagueId),
        'X-User': String(userId),
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify({ type: 'sell', player: playerId, price }),
    })
    if (!response.ok) throw new Error(`Biwenger list player failed: ${response.status}`)
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

  // Joins squad entries against the catalogue and cross-references the
  // market for "is this one of mine that's currently listed" / "what's
  // the standing offer on it, if any" — see docs/biwenger-api-notes.md
  // § "Squad player status". Returns {player, owner, inMarket,
  // offerAmount} tuples rather than a merged object, same reasoning as
  // getCurrentMarket's {sale, player}. Shared by getMySquad (which adds
  // draftedPrice on top) and cycleListings (which doesn't need it).
  const buildSquadTuples = ({ squadEntries, catalogue, sales, offers, userId }) =>
    squadEntries
      .map(({ id, owner }) => {
        const player = catalogue[String(id)]
        if (!player) return null
        const inMarket = sales.some((sale) => sale.user?.id === userId && sale.player.id === id)
        // First matching offer's amount — a player hasn't been observed
        // with more than one standing offer at once; not disambiguated
        // further without a concrete case that needs it.
        const offer = offers.find((offer) => offer.to?.id === userId && offer.requestedPlayers?.includes(id))
        return { player, owner, inMarket, offerAmount: offer?.amount ?? null }
      })
      .filter(Boolean)

  // Log in, resolve league/user, fetch the owned players (with their
  // `owner` data), and join via buildSquadTuples, then add draftedPrice
  // on top — squad-player-view.js does the final shaping.
  const getMySquad = async ({ email, password }) => {
    const token = await login({ email, password })
    const { leagueId, userId } = await getAccount({ token })
    const [squadEntries, catalogue, { sales, offers }] = await Promise.all([
      getSquadEntries({ token, leagueId, userId }),
      getCatalogue(),
      getMarketData({ token, leagueId, userId }),
    ])
    const tuples = buildSquadTuples({ squadEntries, catalogue, sales, offers, userId })

    // Draft-owned players (owner.price absent — never bought) don't
    // carry their market value at draft time anywhere in the squad or
    // market responses; the only place it exists is each player's own
    // price history, keyed by day.
    const draftedEntries = tuples.filter(({ owner }) => owner.price == null)
    const draftedPriceById = new Map(
      await Promise.all(
        draftedEntries.map(async ({ player, owner }) => {
          const prices = await getPlayerPrices({ playerId: player.id })
          const entry = prices.find(([yymmdd]) => yymmdd === yymmddFromUnixSeconds(owner.date))
          return [player.id, entry?.[1] ?? null]
        })
      )
    )

    return tuples.map((tuple) => ({ ...tuple, draftedPrice: draftedPriceById.get(tuple.player.id) ?? null }))
  }

  // Unlists everything currently on the market and lists up to 5 new
  // candidates in their place, in one authenticated session — see
  // docs/backlog/done/cycle-player-listings.md. The squad/market
  // snapshot is taken once, before any write below runs, so the
  // outgoing listings (still `inMarket: true` in that snapshot) are
  // automatically excluded from selectPlayersToList — no special-casing
  // needed to avoid immediately re-listing what this same call just
  // pulled. Writes fire in parallel via allSettled (not .all) — one
  // failing shouldn't abort the rest of the batch, since unlist and list
  // don't depend on each other's completion.
  const cycleListings = async ({ email, password }) => {
    const token = await login({ email, password })
    const { leagueId, userId } = await getAccount({ token })
    const [squadEntries, catalogue, { sales, offers }] = await Promise.all([
      getSquadEntries({ token, leagueId, userId }),
      getCatalogue(),
      getMarketData({ token, leagueId, userId }),
    ])
    const squadTuples = buildSquadTuples({ squadEntries, catalogue, sales, offers, userId })

    const unlisted = sales.filter((sale) => sale.user?.id === userId).map((sale) => sale.player.id)
    const listed = selectPlayersToList(squadTuples)

    await Promise.allSettled([
      ...unlisted.map((playerId) => unlistPlayerData({ token, leagueId, userId, playerId })),
      ...listed.map((playerId) => listPlayerData({ token, leagueId, userId, playerId, price: DEFAULT_LISTING_PRICE })),
    ])

    return { unlisted, listed }
  }

  // Shared by getCurrentMarket and getMyMarketListings: joins a
  // (pre-filtered) slice of `sales` against the catalogue into
  // {sale, player} pairs — see getCurrentMarket's comment for why a pair
  // rather than a merged object.
  const salesToListings = (sales, catalogue) =>
    sales
      .map((sale) => {
        const player = catalogue[String(sale.player.id)]
        return player && { sale, player }
      })
      .filter(Boolean)

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
    return salesToListings(
      sales.filter((sale) => sale.user?.id !== userId),
      catalogue
    )
  }

  // The requester's own listings — same join as getCurrentMarket, just
  // the kept-vs-filtered-out half of the same `sale.user.id` check. See
  // docs/biwenger-api-notes.md § "Squad player status", where
  // enrich-squad-player-cards' "Listed" badge does the same match but
  // only keeps a boolean.
  const getMyMarketListings = async ({ email, password }) => {
    const token = await login({ email, password })
    const { leagueId, userId } = await getAccount({ token })
    const [{ sales }, catalogue] = await Promise.all([
      getMarketData({ token, leagueId, userId }),
      getCatalogue(),
    ])
    return salesToListings(
      sales.filter((sale) => sale.user?.id === userId),
      catalogue
    )
  }

  // Incoming purchase offers on the requester's own squad players — same
  // `GET /market` response as getCurrentMarket/getMyMarketListings, but
  // `data.offers[]` instead of `data.sales[]`. An offer has no embedded
  // player, just `requestedPlayers: [playerId, ...]` — flatMap each into
  // its own {offer, player} pair (one row per offered-on player) rather
  // than a merged object, same reasoning as the sale-based pairs. See
  // docs/biwenger-api-notes.md § "Squad player status" — `to.id` is the
  // offer's recipient (must be the requester), `from` has been null in
  // every sample seen so far (kept on the pair regardless, in case that
  // changes).
  const getOffersOnMyPlayers = async ({ email, password }) => {
    const token = await login({ email, password })
    const { leagueId, userId } = await getAccount({ token })
    const [{ offers }, catalogue] = await Promise.all([
      getMarketData({ token, leagueId, userId }),
      getCatalogue(),
    ])
    return offers
      .filter((offer) => offer.to?.id === userId)
      .flatMap((offer) =>
        offer.requestedPlayers
          .map((playerId) => {
            const player = catalogue[String(playerId)]
            return player && { offer, player }
          })
          .filter(Boolean)
      )
  }

  const rejectOffer = async ({ email, password, offerId }) => {
    const token = await login({ email, password })
    const { leagueId, userId } = await getAccount({ token })
    await rejectOfferData({ token, leagueId, userId, offerId })
  }

  const acceptOffer = async ({ email, password, offerId }) => {
    const token = await login({ email, password })
    const { leagueId, userId } = await getAccount({ token })
    await acceptOfferData({ token, leagueId, userId, offerId })
  }

  const unlistPlayer = async ({ email, password, playerId }) => {
    const token = await login({ email, password })
    const { leagueId, userId } = await getAccount({ token })
    await unlistPlayerData({ token, leagueId, userId, playerId })
  }

  const listPlayer = async ({ email, password, playerId, price }) => {
    const token = await login({ email, password })
    const { leagueId, userId } = await getAccount({ token })
    await listPlayerData({ token, leagueId, userId, playerId, price })
  }

  // My own outgoing bids on other managers' players — same `GET
  // /market` response as getOffersOnMyPlayers, but `data.offers[]`
  // filtered to `offer.from?.id === userId` instead of
  // `offer.to?.id === userId` — the opposite side of the same check.
  // See docs/biwenger-api-notes.md § "Squad player status" (verified
  // 2026-08-22 against a real outgoing bid): `from`/`to` each only ever
  // identify the requester, on whichever side they're on, never the
  // other party — so unlike getOffersOnMyPlayers, the seller/owner
  // *is* identifiable here, by joining against `data.sales[]` (same
  // shape getCurrentMarket/getMyMarketListings join) rather than the
  // catalogue alone. A bid whose player has no matching `sales[]` entry
  // is skipped — not yet observed, and there'd be no asking price to
  // show it against.
  const getMyBidsOnOtherPlayers = async ({ email, password }) => {
    const token = await login({ email, password })
    const { leagueId, userId } = await getAccount({ token })
    const [{ sales, offers }, catalogue] = await Promise.all([
      getMarketData({ token, leagueId, userId }),
      getCatalogue(),
    ])
    return offers
      .filter((offer) => offer.from?.id === userId)
      .flatMap((offer) =>
        offer.requestedPlayers
          .map((playerId) => {
            const sale = sales.find((sale) => sale.player.id === playerId)
            const player = catalogue[String(playerId)]
            return sale && player && { offer, sale, player }
          })
          .filter(Boolean)
      )
  }

  const getLineupData = async ({ token, leagueId, userId }) => {
    const response = await httpFetch(`${baseUrl}/user?fields=lineup(type,playersID)`, {
      headers: {
        Authorization: `Bearer ${token}`,
        'X-League': String(leagueId),
        'X-User': String(userId),
        Accept: 'application/json',
      },
    })
    const { data } = await response.json()
    return data.lineup
  }

  const saveLineupData = async ({ token, leagueId, userId, formation, playerIds }) => {
    const response = await httpFetch(`${baseUrl}/user?fields=lineup(type,playersID)`, {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${token}`,
        'X-League': String(leagueId),
        'X-User': String(userId),
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify({ lineup: { type: formation, playersID: playerIds } }),
    })
    const { data } = await response.json()
    return data.lineup
  }

  // See getLineup's comment for why `null` (vacant, or an id the
  // catalogue doesn't recognize) is kept in place rather than filtered.
  const toLineupPlayers = (playersID, catalogue) =>
    playersID.map((id) => (id == null ? null : catalogue[String(id)] ?? null))

  // Starting lineup — see docs/biwenger-api-notes.md § "Starting
  // lineup". Returns {formation, players, credits} rather than a merged
  // object: `players` here are catalogue players in `playersID`'s order
  // (goalkeeper, then defenders/midfielders/forwards, grouped
  // back-to-front per the formation counts) — lineup-view.js does the
  // shaping into named position groups. `credits` rides along because
  // it's already on the same getAccount call and swap-lineup-players'
  // secondary-position slice needs it to gate off-position fills — see
  // docs/biwenger-api-notes.md § "Starting lineup — write"'s
  // credit-cost note.
  //
  // A vacant slot is `null` in `playersID` (see docs/biwenger-api-notes.md
  // § "Starting lineup — write", confirmed against a real account) — kept
  // as `null` here too, at the same index, rather than filtered out.
  // Filtering it out (the original approach) silently shortened the
  // array and shifted every later slot's band by one, the same
  // misattribution bug the read side already warns about for
  // off-position alignment. An id absent from the catalogue (unexpected,
  // never seen) is treated the same way — `null` in place — for the same
  // reason, rather than spliced out.
  const getLineup = async ({ email, password }) => {
    const token = await login({ email, password })
    const { leagueId, userId, credits } = await getAccount({ token })
    const [lineup, catalogue] = await Promise.all([
      getLineupData({ token, leagueId, userId }),
      getCatalogue(),
    ])
    return {
      formation: lineup.type,
      players: toLineupPlayers(lineup.playersID, catalogue),
      credits,
    }
  }

  // Write side of "Starting lineup" — see docs/biwenger-api-notes.md §
  // "Starting lineup — write". `playerIds` must be the full,
  // fixed-length array the formation expects (goalkeeper, then D/M/F
  // counts, back-to-front, same order `getLineup` returns), `null` at
  // any index left vacant — a shortened array is rejected by Biwenger
  // itself (400, wrong position), it does not mean "vacant" on the
  // write side either. Returns the saved lineup, shaped the same way
  // getLineup does, off Biwenger's own write response rather than a
  // separate follow-up GET.
  //
  // `credits` is re-fetched via a second getAccount AFTER the write,
  // not reused from the pre-write call — an off-position assignment
  // silently deducts credits (see docs/biwenger-api-notes.md § "Starting
  // lineup — write"), and the write response itself gives no hint of
  // it, so the pre-write balance would be stale.
  const saveLineup = async ({ email, password, formation, playerIds }) => {
    const token = await login({ email, password })
    const { leagueId, userId } = await getAccount({ token })
    const [lineup, catalogue] = await Promise.all([
      saveLineupData({ token, leagueId, userId, formation, playerIds }),
      getCatalogue(),
    ])
    const { credits } = await getAccount({ token })
    return {
      formation: lineup.type,
      players: toLineupPlayers(lineup.playersID, catalogue),
      credits,
    }
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

  return {
    getMySquad,
    getCurrentMarket,
    getMyMarketListings,
    getOffersOnMyPlayers,
    rejectOffer,
    acceptOffer,
    unlistPlayer,
    listPlayer,
    cycleListings,
    getMyBidsOnOtherPlayers,
    getLineup,
    saveLineup,
    getPlayerPrices,
    getPlayerGameweekPoints,
  }
}
