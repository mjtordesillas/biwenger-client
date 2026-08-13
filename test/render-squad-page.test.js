import { test } from 'node:test'
import assert from 'node:assert/strict'
import { renderSquadPage } from '../src/render-squad-page.js'

test('renders a row per player with name, position label, and formatted price', () => {
  const html = renderSquadPage({
    players: [{ id: 1, name: 'Brugué', position: 4, price: 280000 }],
  })

  assert.match(html, /Brugué/)
  assert.match(html, /FW/)
  assert.match(html, /280.000/) // es-ES grouping separator
})

test('falls back to the raw position code when it is unrecognized', () => {
  const html = renderSquadPage({
    players: [{ id: 2, name: 'Mystery Player', position: 9, price: 1000 }],
  })

  assert.match(html, /<td>9<\/td>/)
})

test('escapes player names to avoid HTML injection', () => {
  const html = renderSquadPage({
    players: [{ id: 3, name: '<script>alert(1)</script>', position: 1, price: 1000 }],
  })

  assert.doesNotMatch(html, /<script>/)
  assert.match(html, /&lt;script&gt;/)
})
