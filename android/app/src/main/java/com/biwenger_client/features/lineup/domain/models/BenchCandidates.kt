package com.biwenger_client.features.lineup.domain.models

import com.biwenger_client.features.squad.domain.models.SquadPlayer

// The bench players eligible to fill one specific lineup slot — same
// primary position as that slot's band, not already in the starting
// eleven. Secondary-position eligibility (Biwenger lets a manager align
// e.g. a MF/FW as a forward — see docs/biwenger-api-notes.md § "Starting
// lineup") is deliberately out of scope for this slice; narrows who can
// fill a slot, doesn't change what a slot needs to work at all.
data class BenchCandidates(val slotIndex: Int, val players: List<SquadPlayer>)

// lineup.vacant-slot-tapped / lineup.slot-picker-requested's payload —
// which array index needs candidates, for which band's position code.
data class SlotPickerRequest(val index: Int, val position: Int)

// lineup.slot-filled's payload — which array index (not a player id:
// a vacant slot has none) gets which incoming player's id.
data class SlotFillRequest(val index: Int, val playerId: Int)
