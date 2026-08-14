import { test } from 'node:test'
import assert from 'node:assert/strict'
import { toPriceHistoryView } from '../src/price-history-view.js'

test('keeps entries from this season (July 1 onward) when today is mid-season', () => {
  const prices = [
    [250630, 100], // last day of prior season — excluded
    [250701, 105], // season start — included
    [251215, 120],
  ]

  const view = toPriceHistoryView(prices, { today: new Date('2026-01-10T00:00:00Z') })

  assert.deepEqual(view, [
    { date: '2025-07-01', price: 105 },
    { date: '2025-12-15', price: 120 },
  ])
})

test('rolls the season boundary forward once today crosses July 1', () => {
  const prices = [
    [260630, 200], // last day of the still-open prior season — excluded
    [260701, 210], // new season start — included
  ]

  const view = toPriceHistoryView(prices, { today: new Date('2026-08-14T00:00:00Z') })

  assert.deepEqual(view, [{ date: '2026-07-01', price: 210 }])
})

test('returns an empty list when no entries fall within the current season', () => {
  const prices = [[250101, 100], [250630, 110]]

  const view = toPriceHistoryView(prices, { today: new Date('2026-01-10T00:00:00Z') })

  assert.deepEqual(view, [])
})
