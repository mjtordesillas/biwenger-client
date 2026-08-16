import { test } from 'node:test'
import assert from 'node:assert/strict'
import { picasBase } from '../src/as-score.js'

test('maps picas to their base points, per the reverse-engineered table', () => {
  assert.equal(picasBase(0), -2)
  assert.equal(picasBase(1), 2)
  assert.equal(picasBase(2), 6)
  assert.equal(picasBase(3), 10)
  assert.equal(picasBase(4), 14)
})

test('maps the "SC" (unrated) picas value to 0', () => {
  assert.equal(picasBase('SC'), 0)
})

test('returns null for an unrecognized picas value', () => {
  assert.equal(picasBase(undefined), null)
})
