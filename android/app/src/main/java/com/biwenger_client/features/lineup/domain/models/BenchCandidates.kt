package com.biwenger_client.features.lineup.domain.models

import com.biwenger_client.features.squad.domain.models.SquadPlayer

// The bench players eligible to fill one specific lineup slot, split
// the way Biwenger's own lineup editor does — "Specialists" (the
// slot's band as their primary position) and "Jollies" (the band only
// as their secondary position, e.g. a MF/FW aligned as a forward — see
// docs/biwenger-api-notes.md § "Starting lineup"), neither group
// already in the starting eleven. A jolly costs OffPositionCreditCost
// account-wide credits, silently (see docs/biwenger-api-notes.md §
// "Starting lineup — write") — `canAffordJolly` gates whether a jolly
// card is selectable, computed once here rather than re-derived at the
// UI layer.
const val OffPositionCreditCost = 2

data class BenchCandidates(
    val slotIndex: Int,
    val specialists: List<SquadPlayer>,
    val jollies: List<SquadPlayer>,
    val canAffordJolly: Boolean,
)

// lineup.vacant-slot-tapped / lineup.slot-picker-requested's payload —
// which array index needs candidates, for which band's position code.
data class SlotPickerRequest(val index: Int, val position: Int)

// lineup.slot-filled's payload — which array index (not a player id:
// a vacant slot has none) gets which incoming player's id.
data class SlotFillRequest(val index: Int, val playerId: Int)
