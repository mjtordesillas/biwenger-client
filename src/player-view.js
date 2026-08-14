// Shapes a raw Biwenger catalogue player (see docs/rat.md) into what
// clients actually need. Image URLs aren't returned by the API — they're
// built from documented CDN path conventions (verified empirically, see
// commit history) so every client (Android, future web/iOS) shares one
// source of truth instead of reconstructing them itself.
const IMAGE_BASE_URL = 'https://cdn.biwenger.com'

export const toPlayerView = (player) => ({
  id: player.id,
  name: player.name,
  position: player.position,
  secondaryPosition: player.altPositions?.[0] ?? null,
  price: player.price,
  priceIncrement: player.priceIncrement ?? 0,
  points: player.points ?? 0,
  photoUrl: `${IMAGE_BASE_URL}/i/p/${player.id}.png`,
  teamCrestUrl: `${IMAGE_BASE_URL}/i/t/${player.teamID}.png`,
})
