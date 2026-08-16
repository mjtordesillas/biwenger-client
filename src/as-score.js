const PICAS_BASE = { 0: -2, 1: 2, 2: 6, 3: 10, 4: 14, SC: 0 }

export const picasBase = (picas) => PICAS_BASE[picas] ?? null
