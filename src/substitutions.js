const TYPE = { 4: 'substitutedOff', 5: 'substitutedOn' }

export const toSubstitutionRows = (events = []) =>
  events.filter((event) => TYPE[event.type]).map((event) => ({ type: TYPE[event.type], minute: event.metadata }))
