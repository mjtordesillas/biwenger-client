import { test } from 'node:test'
import assert from 'node:assert/strict'
import { toPlayerBidView } from '../src/player-bid-view.js'

test('shapes a {offer, sale, player} triple, using the asking price and exposing the catalogue value/bid amount separately', () => {
  const view = toPlayerBidView({
    offer: { amount: 150000, until: 1787461200, from: { id: 14256124, name: 'Manu' }, to: null, requestedPlayers: [42277], status: 'waiting', type: 'purchase' },
    sale: { date: 1787288871, until: 1787461200, price: 150000, player: { id: 42277 }, user: null },
    player: { id: 42277, name: 'Diarra', teamID: 87, position: 3, price: 200000, priceIncrement: -5000, points: 2 },
  })

  assert.deepEqual(view, {
    id: 42277,
    name: 'Diarra',
    position: 3,
    secondaryPosition: null,
    price: 150000,
    priceIncrement: -5000,
    points: 2,
    photoUrl: 'https://cdn.biwenger.com/i/p/42277.png',
    teamCrestUrl: 'https://cdn.biwenger.com/i/t/87.png',
    marketValue: 200000,
    until: 1787461200,
    seller: null,
    amount: 150000,
  })
})

test('sets seller to the sale\'s owner name for a manager clause-buy', () => {
  const view = toPlayerBidView({
    offer: { amount: 6990000, until: 1787116982, from: { id: 42, name: 'Me' }, to: null, requestedPlayers: [16738], status: 'waiting', type: 'purchase' },
    sale: { date: 1787029492, until: 1787116982, price: 7030000, player: { id: 16738, owner: { clause: 7030000 } }, user: { id: 2873718, name: 'Molina Investments *' } },
    player: { id: 16738, name: 'Courtois', teamID: 15, position: 1, price: 7030000 },
  })

  assert.equal(view.seller, 'Molina Investments *')
})
