import { createSquadHandler } from './squad-handler.js'

// Production wiring lives here, separate from handler logic — see
// docs/coding-conventions/handler-factory-pattern.md.
export const squad = createSquadHandler()
