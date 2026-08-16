import { playerPhotoUrl, teamCrestUrl } from './image-cdn.js'

// Shapes a raw Biwenger catalogue player (see docs/rat.md) into what
// clients actually need.
export const toPlayerView = (player) => ({
  id: player.id,
  name: player.name,
  position: player.position,
  secondaryPosition: player.altPositions?.[0] ?? null,
  price: player.price,
  priceIncrement: player.priceIncrement ?? 0,
  points: player.points ?? 0,
  photoUrl: playerPhotoUrl(player.id),
  teamCrestUrl: teamCrestUrl(player.teamID),
})
