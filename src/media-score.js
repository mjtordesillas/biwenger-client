const roundHalfAwayFromZero = (n) => (n >= 0 ? Math.floor(n + 0.5) : Math.ceil(n - 0.5))

export const mediaPoints = ({ as, sofaScore }) => {
  if (as == null || sofaScore == null) {
    return null
  }
  return roundHalfAwayFromZero((as + sofaScore) / 2)
}
