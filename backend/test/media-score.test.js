import { test } from 'node:test'
import assert from 'node:assert/strict'
import { mediaPoints } from '../src/media-score.js'

test('averages the two totals', () => {
  assert.equal(mediaPoints({ as: 6, sofaScore: 4 }), 5)
  assert.equal(mediaPoints({ as: 12, sofaScore: 6 }), 9)
  assert.equal(mediaPoints({ as: 15, sofaScore: 17 }), 16)
})

test('rounds a positive .5 average up', () => {
  assert.equal(mediaPoints({ as: 5, sofaScore: 14 }), 10)
  assert.equal(mediaPoints({ as: 2, sofaScore: 3 }), 3)
})

test('rounds a negative .5 average away from zero, not toward +Infinity', () => {
  assert.equal(mediaPoints({ as: -4, sofaScore: -1 }), -3)
  assert.equal(mediaPoints({ as: -2, sofaScore: -1 }), -2)
})

test('returns null when either total is missing', () => {
  assert.equal(mediaPoints({ as: null, sofaScore: 4 }), null)
  assert.equal(mediaPoints({ as: 6, sofaScore: null }), null)
})
