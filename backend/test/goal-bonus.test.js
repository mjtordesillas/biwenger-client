import { test } from 'node:test'
import assert from 'node:assert/strict'
import { goalBonus } from '../src/goal-bonus.js'

test('scales the goal bonus by the position played that match', () => {
  assert.equal(goalBonus({ pos1: true }), 6)
  assert.equal(goalBonus({ pos2: true }), 5)
  assert.equal(goalBonus({ pos3: true }), 4)
  assert.equal(goalBonus({ pos4: true }), 3)
})

test('returns null when no position flag is set', () => {
  assert.equal(goalBonus({}), null)
})
