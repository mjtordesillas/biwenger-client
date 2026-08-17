import { test } from 'node:test'
import assert from 'node:assert/strict'
import { toPriceHistoryView } from '../src/price-history-view.js'

test('returns the full trailing window as-is, plus where the current season starts', () => {
  const prices = [
    [250630, 100],
    [250701, 105],
    [251215, 120],
  ]

  const view = toPriceHistoryView(prices, { today: new Date('2026-01-10T00:00:00Z') })

  assert.deepEqual(view, {
    seasonStart: '2025-07-01',
    prices: [
      { date: '2025-06-30', price: 100 },
      { date: '2025-07-01', price: 105 },
      { date: '2025-12-15', price: 120 },
    ],
  })
})

test('rolls the season boundary forward once today crosses July 1, without touching the prices array', () => {
  const prices = [[260630, 200], [260701, 210]]

  const view = toPriceHistoryView(prices, { today: new Date('2026-08-14T00:00:00Z') })

  assert.equal(view.seasonStart, '2026-07-01')
  assert.deepEqual(view.prices, [
    { date: '2026-06-30', price: 200 },
    { date: '2026-07-01', price: 210 },
  ])
})
