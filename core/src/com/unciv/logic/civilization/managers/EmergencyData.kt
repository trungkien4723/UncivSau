package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization

enum class EmergencyType {
    Military,
    Religious,
    AidRequest,
    Nuclear,
    Climate
}

data class EmergencyData(
    val type: EmergencyType,
    val targetCivId: String,
    val triggerTurn: Int,
    val duration: Int,
    val participantCivIds: MutableSet<String> = mutableSetOf(),
    val contributions: MutableMap<String, Int> = mutableMapOf(),
    var isResolved: Boolean = false
) : IsPartOfGameInfoSerialization
