package com.unciv.logic.battle

import com.unciv.logic.civilization.Civilization
import com.unciv.logic.map.mapunit.MapUnit
import com.unciv.logic.map.tile.Tile
import com.unciv.models.UncivSound
import com.unciv.models.ruleset.unique.GameContext
import com.unciv.models.ruleset.unique.Unique
import com.unciv.models.ruleset.unique.UniqueType
import com.unciv.models.ruleset.unit.UnitType
import yairm210.purity.annotations.Readonly

class MapUnitCombatant(val unit: MapUnit) : ICombatant {
    override fun getHealth(): Int = unit.health
    override fun getMaxHealth() = 100
    override fun getCivInfo(): Civilization = unit.civ
    override fun getTile(): Tile = unit.getTile()
    override fun getName(): String = unit.name
    override fun isDefeated(): Boolean = unit.health <= 0
    override fun isInvisible(to: Civilization): Boolean = unit.isInvisible(to)
    override fun canAttack(): Boolean = unit.canAttack()
    override fun matchesFilter(filter: String, multiFilter: Boolean) = unit.matchesFilter(filter, multiFilter)
    override fun getAttackSound() = unit.baseUnit.attackSound.let {
        if (it == null) UncivSound.Click else UncivSound(it)
    }

    override fun getNotificationDisplay(leadingText: String): String {
        val isUnitUnnamed = unit.instanceName.isNullOrEmpty()
        return if (isUnitUnnamed)
            leadingText + "[" + unit.name + "]"
        else
            "[" + unit.instanceName + "]"
    }


    override fun takeDamage(damage: Int) = unit.takeDamage(damage)

    override fun getAttackingStrength(defender: ICombatant?, combatAction: CombatAction): Int {
        val state = GameContext(this, defender, this.getTile(), combatAction)
        val extraStrength = unit.getMatchingUniques(UniqueType.StrengthAmount, state).sumOf { it.params[0].toInt() }
        val formationBonus = getFormationBonus()
        val baseStrength = if (isRanged()) unit.baseUnit.rangedStrength else if (combatAction == CombatAction.TheologicalCombat) unit.baseUnit.religiousStrength else unit.baseUnit.strength
        return baseStrength + extraStrength + formationBonus
    }

    override fun getDefendingStrength(attacker: ICombatant?, combatAction: CombatAction): Int {
        val attackedByRanged = attacker?.isRanged() == true
        val state = GameContext(this, attacker, this.getTile(), combatAction)
        val extraStrength = unit.getMatchingUniques(UniqueType.StrengthAmount, state).sumOf { it.params[0].toInt() }
        val formationBonus = getFormationBonus()
        val baseStrength = when {
            combatAction == CombatAction.TheologicalCombat -> unit.baseUnit.religiousStrength
            unit.isEmbarked() && !isCivilian() -> unit.civ.getEra().embarkDefense
            isRanged() && attackedByRanged -> unit.baseUnit.rangedStrength
            else -> unit.baseUnit.strength
        }
        return baseStrength + extraStrength + formationBonus
    }

    @Readonly private fun getFormationBonus(): Int {
        return when (unit.formationLevel) {
            1 -> 10  // Corps/Fleet: +10 strength
            2 -> 17  // Army/Armada: +17 strength
            else -> 0
        }
    }

    override fun getUnitType(): UnitType {
        return unit.type
    }

    override fun toString(): String {
        return unit.name + " of " + unit.civ.civID
    }

    @Readonly 
    fun getMatchingUniques(uniqueType: UniqueType, gameContext: GameContext, checkCivUniques: Boolean): Sequence<Unique> =
        unit.getMatchingUniques(uniqueType, gameContext, checkCivUniques)

    @Readonly
    override fun getTriggeredUniques(
        trigger: UniqueType,
        gameContext: GameContext,
        triggerFilter: (Unique) -> Boolean
    ): Sequence<Unique> {
        return unit.getTriggeredUniques(trigger, gameContext, triggerFilter)
    }

    @Readonly
    fun hasUnique(uniqueType: UniqueType, conditionalState: GameContext? = null): Boolean =
        if (conditionalState == null) unit.hasUnique(uniqueType)
        else unit.hasUnique(uniqueType, conditionalState)
    
    @Readonly
    override fun hashCode() = unit.hashCode()
    @Readonly
    override fun equals(other: Any?) = other is MapUnitCombatant && other.unit == unit


}
