import { test } from 'node:test'
import assert from 'node:assert/strict'
import { toMarketListingView } from '../src/market-listing-view.js'

test('shapes a {sale, player} pair, using the asking price and exposing the catalogue value separately', () => {
  const view = toMarketListingView({
    sale: { price: 6990000, until: 1787116982, user: { id: 2873718, name: 'Molina Investments *' } },
    player: {
      id: 16738, name: 'Courtois', teamID: 15, position: 1, price: 7030000, priceIncrement: 40000, points: 0,
    },
  })

  assert.deepEqual(view, {
    id: 16738,
    name: 'Courtois',
    position: 1,
    secondaryPosition: null,
    price: 6990000,
    priceIncrement: 40000,
    points: 0,
    photoUrl: 'https://cdn.biwenger.com/i/p/16738.png',
    teamCrestUrl: 'https://cdn.biwenger.com/i/t/15.png',
    marketValue: 7030000,
    until: 1787116982,
    seller: 'Molina Investments *',
  })
})

test('sets seller to null for a free-agent listing (sale.user is null)', () => {
  const view = toMarketListingView({
    sale: { price: 200000, until: 1787029200, user: null },
    player: { id: 10650, name: 'Losada', teamID: 87, position: 4, price: 200000 },
  })

  assert.equal(view.seller, null)
})
