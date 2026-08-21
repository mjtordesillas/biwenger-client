import { test } from 'node:test'
import assert from 'node:assert/strict'
import { toPlayerOfferView } from '../src/player-offer-view.js'

test('shapes a {offer, player} pair, carrying the offer amount, expiry, and bidder through', () => {
  const view = toPlayerOfferView({
    offer: {
      amount: 300000, until: 1787115600, from: { id: 12, name: 'Rival FC' }, to: { id: 42 },
      requestedPlayers: [16738], status: 'waiting', type: 'purchase',
    },
    player: { id: 16738, name: 'Courtois', teamID: 15, position: 1, price: 280000, priceIncrement: 10000, points: 5 },
  })

  assert.deepEqual(view, {
    id: 16738,
    name: 'Courtois',
    position: 1,
    secondaryPosition: null,
    price: 280000,
    priceIncrement: 10000,
    points: 5,
    photoUrl: 'https://cdn.biwenger.com/i/p/16738.png',
    teamCrestUrl: 'https://cdn.biwenger.com/i/t/15.png',
    amount: 300000,
    until: 1787115600,
    bidder: 'Rival FC',
  })
})

test('sets bidder to null when the offer\'s `from` is null (the observed case so far)', () => {
  const view = toPlayerOfferView({
    offer: { amount: 200000, until: 1787115600, from: null, to: { id: 42 }, requestedPlayers: [10650], status: 'waiting', type: 'purchase' },
    player: { id: 10650, name: 'Losada', teamID: 87, position: 4, price: 200000 },
  })

  assert.equal(view.bidder, null)
})
