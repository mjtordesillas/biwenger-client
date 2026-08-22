import { test } from 'node:test'
import assert from 'node:assert/strict'
import { selectPlayersToList } from '../src/biwenger-client.js'

const aTuple = ({ id, offerAmount = null, inMarket = false, lockedUntil = null }) => ({
  player: { id },
  owner: { lockedUntil },
  inMarket,
  offerAmount,
})

test('excludes players already in market or still locked', () => {
  const available = aTuple({ id: 1 })
  const listed = aTuple({ id: 2, inMarket: true })
  const locked = aTuple({ id: 3, lockedUntil: 9999999999 })

  const selected = selectPlayersToList([available, listed, locked])

  assert.deepEqual(selected, [1])
})

test('prefers players without a standing offer, falling back to offered ones only to fill remaining slots', () => {
  const offered = aTuple({ id: 1, offerAmount: 500000 })
  const plain = aTuple({ id: 2 })

  const selected = selectPlayersToList([offered, plain], 5)

  assert.deepEqual(selected, [2, 1])
})

test('caps at maxListings, preferring no-offer candidates first', () => {
  const withoutOffer = [1, 2, 3].map((id) => aTuple({ id }))
  const withOffer = [4, 5, 6].map((id) => aTuple({ id, offerAmount: 100000 }))

  const selected = selectPlayersToList([...withoutOffer, ...withOffer], 4)

  assert.deepEqual(selected, [1, 2, 3, 4])
})
