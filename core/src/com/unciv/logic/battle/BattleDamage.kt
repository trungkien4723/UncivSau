package com.unciv.logic.battle

import com.unciv.logic.map.tile.Tile
import com.unciv.models.Counter
import com.unciv.models.ruleset.GlobalUniques
import com.unciv.models.ruleset.unique.GameContext
import com.unciv.models.ruleset.unique.Unique
import com.unciv.models.ruleset.unique.UniqueTarget
import com.unciv.models.ruleset.unique.UniqueType
import com.unciv.models.translations.tr
import com.unciv.ui.components.extensions.toPercent
import yairm210.purity.annotations.LocalState
import yairm210.purity.annotations.Pure
import yairm210.purity.annotations.Readonly
import kotlin.collections.set
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

object BattleDamage {

    @Readonly
    private fun getModifierStringFromUnique(unique: Unique): String {
        val source = when (unique.sourceObjectType) {
            UniqueTarget.Unit -> "Unit ability"
            UniqueTarget.Nation -> "National ability"
            UniqueTarget.Global -> GlobalUniques.getUniqueSourceDescription(unique)
            else -> "[${unique.sourceObjectName}] ([${unique.getSourceNameForUser()}])"
        }.tr()
        if (unique.modifiers.isEmpty()) return source

        val conditionalsText = unique.modifiers.joinToString { it.text.tr() }
        return "$source - $conditionalsText"
    }

    @Readonly
    private fun getGeneralModifiers(combatant: ICombatant, enemy: ICombatant, combatAction: CombatAction, tileToAttackFrom: Tile): Counter<String> {
        val modifiers = Counter<String>()

        val conditionalState = getGameContext(combatAction, combatant, enemy)
        val civInfo = combatant.getCivInfo()

        if (combatant is MapUnitCombatant) {

            val unitUniqueModifiers = getUnitUniqueModifiers(combatant, enemy, conditionalState, tileToAttackFrom)
            modifiers.add(unitUniqueModifiers)

            val civResources = civInfo.getCivResourcesByName()
            for (resource in combatant.unit.getResourceRequirementsPerTurn().keys)
                if (civResources[resource]!! < 0 && !civInfo.isBarbarian)
                    modifiers["Missing resource"] = BattleConstants.MISSING_RESOURCES_MALUS

            val (greatGeneralName, greatGeneralBonus) = GreatGeneralImplementation.getGreatGeneralBonus(combatant, enemy, combatAction)
            if (greatGeneralBonus != 0)
                modifiers[greatGeneralName] = greatGeneralBonus

        } else if (combatant is CityCombatant) {
            for (unique in combatant.city.getMatchingUniques(UniqueType.StrengthForCities, conditionalState)) {
                modifiers.add(getModifierStringFromUnique(unique), unique.params[0].toInt())
            }
        }

        if (enemy.getCivInfo().isBarbarian) {
            modifiers["Difficulty"] =
                (civInfo.gameInfo.getDifficulty().barbarianBonus * 100).toInt()
        }

        // War Support (Civ VI) — combat modifier when at war
        if (!civInfo.isBarbarian && !enemy.getCivInfo().isBarbarian
            && civInfo.isAtWarWith(enemy.getCivInfo())) {
            val warSupport = civInfo.getDiplomacyManager(enemy.getCivInfo())?.warSupport ?: 0
            if (warSupport != 0) {
                modifiers["War Support"] = warSupport * 5  // Each point = 5% combat strength
            }
        }

        return modifiers
    }

    @Readonly
    private fun getGameContext(
        combatAction: CombatAction,
        combatant: ICombatant,
        enemy: ICombatant,
    ): GameContext {
        val attackedTile =
            if (combatAction == CombatAction.Attack) enemy.getTile()
            else combatant.getTile()

        val conditionalState = GameContext(
            combatant.getCivInfo(),
            city = (combatant as? CityCombatant)?.city,
            ourCombatant = combatant,
            theirCombatant = enemy,
            attackedTile = attackedTile,
            combatAction = combatAction
        )
        return conditionalState
    }

    @Readonly
    private fun getUnitUniqueModifiers(combatant: MapUnitCombatant, enemy: ICombatant, conditionalState: GameContext,
                                       tileToAttackFrom: Tile): Counter<String> {
        val civInfo = combatant.getCivInfo()
        val modifiers = Counter<String>()

        for (unique in combatant.getMatchingUniques(UniqueType.Strength, conditionalState, true)) {
            modifiers.add(getModifierStringFromUnique(unique), unique.params[0].toInt())
        }

        // e.g., Mehal Sefari https://civilization.fandom.com/wiki/Mehal_Sefari_(Civ5)
        for (unique in combatant.getMatchingUniques(
            UniqueType.StrengthNearCapital, conditionalState, true
        )) {
            if (civInfo.cities.isEmpty() || civInfo.getCapital() == null) break
            val distance =
                combatant.getTile().aerialDistanceTo(civInfo.getCapital()!!.getCenterTile())
            // https://steamcommunity.com/sharedfiles/filedetails/?id=326411722#464287
            val effect = unique.params[0].toInt() - 3 * distance
            if (effect > 0)
                modifiers.add(getModifierStringFromUnique(unique), effect)
        }

        //https://www.carlsguides.com/strategy/civilization5/war/combatbonuses.php
        var adjacentUnits = combatant.getTile().neighbors.flatMap { it.getUnits() }
        if (enemy.getTile() !in combatant.getTile().neighbors && tileToAttackFrom in combatant.getTile().neighbors
            && enemy is MapUnitCombatant
        )
            adjacentUnits += sequenceOf(enemy.unit)

        // e.g., Maori Warrior - https://civilization.fandom.com/wiki/Maori_Warrior_(Civ5)
        val strengthMalus = adjacentUnits.filter { it.civ.isAtWarWith(combatant.getCivInfo()) }
            .flatMap { it.getMatchingUniques(UniqueType.StrengthForAdjacentEnemies) }
            .filter { combatant.matchesFilter(it.params[1]) && combatant.getTile().matchesFilter(it.params[2]) }
            .maxByOrNull { it.params[0] }
        if (strengthMalus != null) {
            modifiers.add("Adjacent enemy units", strengthMalus.params[0].toInt())
        }
        return modifiers
    }

    @Readonly
    fun getAttackModifiers(
        attacker: ICombatant,
        defender: ICombatant, tileToAttackFrom: Tile,
        combatAction: CombatAction = CombatAction.Attack
    ): Counter<String> {
        @LocalState val modifiers = getGeneralModifiers(attacker, defender, combatAction, tileToAttackFrom)

        if (attacker is MapUnitCombatant) {

            val terrainAttackModifiers = getTerrainAttackModifiers(attacker, defender, tileToAttackFrom)
            modifiers.add(terrainAttackModifiers)

            // Air unit attacking with Air Sweep
            if (attacker.unit.isPreparingAirSweep())
                modifiers.add(getAirSweepAttackModifiers(attacker))

            if (attacker.isMelee()) {
                val numberOfOtherAttackersSurroundingDefender = defender.getTile().neighbors.count {
                    it.militaryUnit != null && it.militaryUnit != attacker.unit
                            && it.militaryUnit!!.civ == attacker.getCivInfo()
                            && MapUnitCombatant(it.militaryUnit!!).isMelee()
                }
                if (numberOfOtherAttackersSurroundingDefender > 0) {
                    var flankingBonus = BattleConstants.BASE_FLANKING_BONUS

                    // e.g., Discipline policy - https://civilization.fandom.com/wiki/Discipline_(Civ5)
                    for (unique in attacker.unit.getMatchingUniques(UniqueType.FlankAttackBonus, checkCivInfoUniques = true,
                            gameContext = getGameContext(CombatAction.Attack, attacker, defender)))
                        flankingBonus *= unique.params[0].toPercent()
                    modifiers["Flanking"] =
                        (flankingBonus * numberOfOtherAttackersSurroundingDefender).toInt()
                }
            }

            // Civ VI support units: check for friendly Battering Ram / Siege Tower adjacent to defender
            if (defender.isCity()) {
                val supportBonus = getSupportUnitBonus(attacker, defender)
                if (supportBonus != null) {
                    modifiers[supportBonus.first] = supportBonus.second
                }
            }

        }

        return modifiers
    }

    @Readonly
    private fun getSupportUnitBonus(attacker: ICombatant, defender: ICombatant): Pair<String, Int>? {
        if (attacker !is MapUnitCombatant || !attacker.isMelee()) return null

        val defenderTile = defender.getTile()
        for (neighbor in defenderTile.neighbors) {
            for (unit in neighbor.getUnits()) {
                if (unit.civ != attacker.getCivInfo() || unit.isDestroyed) continue
                if (unit.name == "Battering Ram") return Pair("Ram support", 50)
                if (unit.name == "Siege Tower") return Pair("Tower support", 100)
            }
        }
        return null
    }

    @Readonly
    private fun getTerrainAttackModifiers(attacker: MapUnitCombatant, defender: ICombatant, tileToAttackFrom: Tile): Counter<String> {
        val modifiers = Counter<String>()
        if (attacker.unit.isEmbarked() && defender.getTile().isLand
            && !attacker.unit.hasUnique(UniqueType.AttackAcrossCoast)
        )
            modifiers["Landing"] = BattleConstants.LANDING_MALUS

        // Land Melee Unit attacking to Water
        if (attacker.unit.type.isLandUnit() && !attacker.getTile().isWater && attacker.isMelee() && defender.getTile().isWater
            && !attacker.unit.hasUnique(UniqueType.AttackAcrossCoast)
        )
            modifiers["Boarding"] = BattleConstants.BOARDING_MALUS

        // Melee Unit on water attacking to Land (not City) unit
        if (!attacker.unit.type.isAirUnit() && attacker.isMelee() && attacker.getTile().isWater && !defender.getTile().isWater
            && !attacker.unit.hasUnique(UniqueType.AttackAcrossCoast) && !defender.isCity()
        )
            modifiers["Landing"] = BattleConstants.LANDING_MALUS

        if (isMeleeAttackingAcrossRiverWithNoBridge(attacker, tileToAttackFrom, defender))
            modifiers["Across river"] = BattleConstants.ATTACKING_ACROSS_RIVER_MALUS
        return modifiers
    }

    @Readonly
    private fun isMeleeAttackingAcrossRiverWithNoBridge(attacker: MapUnitCombatant, tileToAttackFrom: Tile, defender: ICombatant) = (
        attacker.isMelee()
            &&
            (tileToAttackFrom.aerialDistanceTo(defender.getTile()) == 1
                && tileToAttackFrom.isConnectedByRiver(defender.getTile())
                && !attacker.unit.hasUnique(UniqueType.AttackAcrossRiver))
            &&
            (!tileToAttackFrom.hasConnection(attacker.getCivInfo()) // meaning, the tiles are not road-connected for this civ
                || !defender.getTile().hasConnection(attacker.getCivInfo())
                || !attacker.getCivInfo().tech.roadsConnectAcrossRivers)
        )

    @Readonly
    fun getAirSweepAttackModifiers(
        attacker: ICombatant
    ): Counter<String> {
        val modifiers = Counter<String>()

        if (attacker is MapUnitCombatant) {
            for (unique in attacker.unit.getMatchingUniques(UniqueType.StrengthWhenAirsweep)) {
                modifiers.add(getModifierStringFromUnique(unique), unique.params[0].toInt())
            }
        }

        return modifiers
    }

    @Readonly
    fun getDefenceModifiers(attacker: ICombatant, defender: ICombatant, tileToAttackFrom: Tile, combatAction: CombatAction = CombatAction.Defend): Counter<String> {
        @LocalState val modifiers = getGeneralModifiers(defender, attacker, combatAction, tileToAttackFrom)
        val tile = defender.getTile()

        if (defender is MapUnitCombatant && !defender.unit.isEmbarked()) { // Embarked units get no terrain defensive bonuses

            val tileDefenceBonus = tile.getDefensiveBonus(unit = defender.unit)
            if (!defender.unit.hasUnique(UniqueType.NoDefensiveTerrainBonus, checkCivInfoUniques = true) && tileDefenceBonus > 0
                || !defender.unit.hasUnique(UniqueType.NoDefensiveTerrainPenalty, checkCivInfoUniques = true) && tileDefenceBonus < 0
            )
                modifiers["Tile"] = (tileDefenceBonus * 100).toInt()


            if (defender.unit.isFortified() || defender.unit.isGuarding())
                modifiers["Fortification"] = BattleConstants.FORTIFICATION_BONUS * defender.unit.getFortificationTurns()
        }

        return modifiers
    }

    @Readonly
    private fun modifiersToFinalBonus(modifiers: Counter<String>): Float {
        var finalModifier = 1f
        for (modifierValue in modifiers.values) finalModifier += modifierValue / 100f
        return finalModifier
    }

    @Readonly
    private fun getHealthDependantDamageRatio(combatant: ICombatant): Float {
        return if (combatant !is MapUnitCombatant
            || combatant.unit.hasUnique(UniqueType.NoDamagePenaltyWoundedUnits, checkCivInfoUniques = true)
        ) 1f
        // Each 3 points of health reduces damage dealt by 1%
        else 1 - (100 - combatant.getHealth()) / BattleConstants.DAMAGE_REDUCTION_WOUNDED_UNIT_RATIO_PERCENTAGE
    }


    /**
     * Includes attack modifiers
     */
    @Readonly
    fun getAttackingStrength(
        attacker: ICombatant,
        defender: ICombatant,
        tileToAttackFrom: Tile,
        combatAction: CombatAction = CombatAction.Attack
    ): Float {
        val attackModifier = modifiersToFinalBonus(getAttackModifiers(attacker, defender, tileToAttackFrom, combatAction))
        val baseStrength = max(1f, attacker.getAttackingStrength(defender, combatAction) * attackModifier)
        // Civ VI combat-class counter system: flat Combat Strength bonuses/penalties
        return max(1f, baseStrength + getCombatClassAdjustment(attacker, defender, combatAction))
    }

    /** Unit-type names of the two cavalry classes (Light = Mounted, Heavy = Armored). */
    private val CAVALRY_UNIT_TYPES = setOf("Mounted", "Armored")
    /** Unit-type names of the melee classes. */
    private val MELEE_UNIT_TYPES = setOf("Sword", "Gunpowder")

    /**
     * Civ VI counter system (flat Combat Strength adjustments on top of percent modifiers):
     *
     * - **Melee (+5) vs Anti-Cavalry** - Swordsmen break Spearman/Pikeman lines.
     * - **Anti-Cavalry (+10) vs Light & Heavy Cavalry** - Spearmen hold against Horsemen/Knights.
     * - **Siege (-17 Bombard Strength) attacking Land units** - siege is for walls, not field armies.
     *
     * Ranged units attack cities and naval targets at full strength (as in Civ VI) - the
     * earlier -17 penalties here made early ranged units useless against walls.
     */
    @Readonly
    fun getCombatClassAdjustment(attacker: ICombatant, defender: ICombatant, combatAction: CombatAction): Int {
        if (combatAction == CombatAction.TheologicalCombat) return 0
        if (attacker !is MapUnitCombatant) return 0

        val attackerType = attacker.unit.baseUnit.unitType
        val defenderCombatant = defender as? MapUnitCombatant
        val defenderType = defenderCombatant?.unit?.baseUnit?.unitType

        // --- The counter triangle (flat bonuses) ---
        if (attackerType in MELEE_UNIT_TYPES && defenderType == "Anti Cavalry")
            return 5
        if (attackerType == "Anti Cavalry" && defenderType in CAVALRY_UNIT_TYPES)
            return 10

        // --- Class penalties (-17 Strength) ---
        if (!attacker.isRanged() || attacker.isAirUnit()) return 0
        return when {
            // Siege attacking field armies is heavily penalized without escort support -
            // siege is FOR breaking walls, so vs cities it fights at full Bombard Strength
            defenderCombatant != null && defenderCombatant.unit.baseUnit.isLandUnit ->
                if (attackerType == "Siege") -17 else 0
            else -> 0
        }
    }


    /**
     * Includes defence modifiers
     */
    @Readonly
    fun getDefendingStrength(attacker: ICombatant, defender: ICombatant, tileToAttackFrom: Tile, combatAction: CombatAction = CombatAction.Defend): Float {
        val defenceModifier = modifiersToFinalBonus(getDefenceModifiers(attacker, defender, tileToAttackFrom, combatAction))
        return max(1f, defender.getDefendingStrength(attacker, combatAction) * defenceModifier)
    }
    
    @Readonly
    fun getRandomness(combatant: ICombatant): Float = 
        Random(combatant.getCivInfo().gameInfo.turns
                * combatant.getTile().position.toVector2().hashCode().toLong()).nextFloat()

    @Readonly
    fun calculateDamageToAttacker(
        attacker: ICombatant,
        defender: ICombatant,
        tileToAttackFrom: Tile = defender.getTile(),
        /** Between 0 and 1. */
        randomnessFactor: Float = getRandomness(attacker),
        combatAction: CombatAction = CombatAction.Attack
    ): Int {
        if (attacker.isRanged() && !attacker.isAirUnit()) return 0
        if (defender.isCivilian()) return 0
        val strengthDifference = getAttackingStrength(attacker, defender, tileToAttackFrom, combatAction) -
                getDefendingStrength(attacker, defender, tileToAttackFrom, combatAction)
        return (damageModifier(strengthDifference, randomnessFactor) * getHealthDependantDamageRatio(defender)).roundToInt()
    }

    @Readonly
    fun calculateDamageToDefender(
        attacker: ICombatant,
        defender: ICombatant,
        tileToAttackFrom: Tile = defender.getTile(),
        /** Between 0 and 1.  Defaults to turn and location-based random to avoid save scumming */
        randomnessFactor: Float = getRandomness(defender)
        ,
        combatAction: CombatAction = CombatAction.Attack
    ): Int {
        if (defender.isCivilian()) return BattleConstants.DAMAGE_TO_CIVILIAN_UNIT
        val strengthDifference = getAttackingStrength(attacker, defender, tileToAttackFrom, combatAction) -
                getDefendingStrength(attacker, defender, tileToAttackFrom, combatAction)
        return (damageModifier(strengthDifference, randomnessFactor) * getHealthDependantDamageRatio(attacker)).roundToInt()
    }

    /** Civ VI: damage scales exponentially with the combat strength difference - each point of
     *  advantage multiplies damage by e^0.04 (~+4%). Equal strengths deal ~30 of 100 HP, a +15
     *  advantage ~55, and a one-shot needs roughly a +26 to +36 gap. */
    @Pure
    private fun damageModifier(
        strengthDifference: Float,
        /** Between 0 and 1. */
        randomnessFactor: Float,
    ): Float {
        val randomCenteredAround30 = 24 + 12 * randomnessFactor
        return randomCenteredAround30 * exp(0.04f * strengthDifference)
    }
}
enum class CombatAction {
    Attack,
    Defend,
    Intercept,
    TheologicalCombat,
}
