// Player photos and team crests aren't returned by any endpoint field —
// built from path conventions, verified empirically. See
// docs/biwenger-api-notes.md § "Image CDN". Shared by player-view.js (a
// player's own team crest) and match-day-details-view.js (both teams in a
// match), so it's modeled once rather than templated per view.
const IMAGE_BASE_URL = 'https://cdn.biwenger.com'

export const playerPhotoUrl = (playerId) => `${IMAGE_BASE_URL}/i/p/${playerId}.png`

export const teamCrestUrl = (teamId) => `${IMAGE_BASE_URL}/i/t/${teamId}.png`
