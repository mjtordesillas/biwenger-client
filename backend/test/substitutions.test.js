import { test } from 'node:test'
import assert from 'node:assert/strict'
import { toSubstitutionRows } from '../src/substitutions.js'

test('maps a substituted-off event to a row with its minute', () => {
  const rows = toSubstitutionRows([{ type: 4, period: 'secondTime', metadata: 70 }])

  assert.deepEqual(rows, [{ type: 'substitutedOff', minute: 70 }])
})

test('maps a substituted-on event to a row with its minute', () => {
  const rows = toSubstitutionRows([{ type: 5, period: 'secondTime', metadata: 62 }])

  assert.deepEqual(rows, [{ type: 'substitutedOn', minute: 62 }])
})

test('ignores non-substitution events (e.g. a goal)', () => {
  const rows = toSubstitutionRows([{ type: 1, period: 'firstTime', metadata: 26 }])

  assert.deepEqual(rows, [])
})

test('keeps both rows when a player is subbed on and later off in the same match', () => {
  const rows = toSubstitutionRows([
    { type: 5, period: 'secondTime', metadata: 45 },
    { type: 4, period: 'secondTime', metadata: 80 },
  ])

  assert.deepEqual(rows, [
    { type: 'substitutedOn', minute: 45 },
    { type: 'substitutedOff', minute: 80 },
  ])
})

test('returns an empty array when there are no events', () => {
  assert.deepEqual(toSubstitutionRows(), [])
  assert.deepEqual(toSubstitutionRows([]), [])
})
