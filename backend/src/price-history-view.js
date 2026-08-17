const seasonStart = (today) => {
  const year = today.getUTCMonth() >= 6 ? today.getUTCFullYear() : today.getUTCFullYear() - 1
  return Date.UTC(year, 6, 1)
}

const parseYYMMDD = (yymmdd) => {
  const digits = String(yymmdd).padStart(6, '0')
  const year = 2000 + Number(digits.slice(0, 2))
  const month = Number(digits.slice(2, 4)) - 1
  const day = Number(digits.slice(4, 6))
  return Date.UTC(year, month, day)
}

const toIsoDate = (timestamp) => new Date(timestamp).toISOString().slice(0, 10)

export const toPriceHistoryView = (prices, dependencies = {}) => {
  const { today = new Date() } = dependencies
  return {
    seasonStart: toIsoDate(seasonStart(today)),
    prices: prices.map(([yymmdd, price]) => ({ date: toIsoDate(parseYYMMDD(yymmdd)), price })),
  }
}
