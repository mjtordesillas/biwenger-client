// Biwenger's catalogue position codes: 1=GK 2=DF 3=MF 4=FW.
// Confirmed against one real player in the RAT (docs/rat.md); worth a
// second look once more of the squad is visible.
const POSITION_LABELS = { 1: 'GK', 2: 'DF', 3: 'MF', 4: 'FW' }

const formatPrice = (price) =>
  new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
    maximumFractionDigits: 0,
  }).format(price)

const HTML_ESCAPES = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }
const escapeHtml = (value) => String(value).replace(/[&<>"']/g, (character) => HTML_ESCAPES[character])

const renderRow = (player) => `
        <tr>
          <td>${escapeHtml(player.name)}</td>
          <td>${escapeHtml(POSITION_LABELS[player.position] ?? player.position)}</td>
          <td>${escapeHtml(formatPrice(player.price))}</td>
        </tr>`

export const renderSquadPage = ({ players }) => `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>My Squad</title>
  <style>
    body { font-family: system-ui, sans-serif; margin: 0; padding: 1rem; color: #111; }
    table { width: 100%; border-collapse: collapse; }
    td { padding: 0.6rem 0.25rem; border-bottom: 1px solid #ddd; }
    td:last-child { text-align: right; white-space: nowrap; }
  </style>
</head>
<body>
  <table>
    <tbody>${players.map(renderRow).join('')}
    </tbody>
  </table>
</body>
</html>
`
