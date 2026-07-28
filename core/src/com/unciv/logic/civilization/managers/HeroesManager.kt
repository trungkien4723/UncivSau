package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.NotificationCategory
import com.unciv.logic.map.mapunit.MapUnit
import yairm210.purity.annotations.Readonly
import kotlin.math.max

data class HeroData(
    val heroName: String,
    val turnsUntilRetirement: Int,
    val cooldownTurns: Int = 0,
    val isRetired: Boolean = false
) : IsPartOfGameInfoSerialization

class HeroesManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

    var heroes = mutableListOf<HeroData>()
    var maxHeroes = 4
    var heroRetirementTurns = 30
    var heroReviveCooldown = 10

    fun clone(): HeroesManager {
        val toReturn = HeroesManager()
        toReturn.heroes.addAll(heroes.map { it.copy() })
        toReturn.maxHeroes = maxHeroes
        toReturn.heroRetirementTurns = heroRetirementTurns
        toReturn.heroReviveCooldown = heroReviveCooldown
        return toReturn
    }

    fun setTransients(civInfo: Civilization) {
        this.civInfo = civInfo
    }

    @Readonly
    fun canRecruitHero(): Boolean = heroes.size < maxHeroes

    @Readonly
    fun isHeroUnit(unit: MapUnit): Boolean {
        return heroes.any { it.heroName == unit.name && !it.isRetired }
    }

    @Readonly
    fun isHeroName(unitName: String): Boolean = heroes.any { it.heroName == unitName }

    fun recruitHero(heroUnitName: String): Boolean {
        if (!canRecruitHero()) return false
        if (isHeroName(heroUnitName)) return false

        val ruleset = civInfo.gameInfo.ruleset
        if (!ruleset.units.containsKey(heroUnitName)) return false

        heroes.add(HeroData(heroUnitName, heroRetirementTurns))
        civInfo.gameModes.spawnHero(heroUnitName)

        civInfo.addNotification(
            "You have recruited the Hero [$heroUnitName]!",
            NotificationCategory.General,
            "StatIcons/GreatPerson"
        )
        return true
    }

    /**
     * When a hero unit is "defeated" (health reaches 0), instead of dying,
     * it goes on cooldown. After the cooldown, it can be revived.
     */
    fun defeatHero(heroUnit: MapUnit): Boolean {
        val hero = heroes.firstOrNull { it.heroName == heroUnit.name } ?: return false
        if (hero.isRetired) return false

        hero.isRetired = true
        hero.cooldownTurns = heroReviveCooldown

        civInfo.addNotification(
            "Your Hero [$heroUnit.name] has been defeated! It will be available again in [$heroReviveCooldown] turns.",
            heroUnit.currentTile.position,
            NotificationCategory.Units,
            heroUnit.name
        )
        return true
    }

    /**
     * Check if a hero can be revived (cooldown expired).
     */
    @Readonly
    fun canReviveHero(heroName: String): Boolean {
        val hero = heroes.firstOrNull { it.heroName == heroName } ?: return false
        return hero.isRetired && hero.cooldownTurns <= 0
    }

    /**
     * Revive a hero, creating a new unit.
     */
    fun reviveHero(heroName: String): Boolean {
        if (!canReviveHero(heroName)) return false
        val hero = heroes.first { it.heroName == heroName }

        val capital = civInfo.getCapital()
        if (capital == null) return false

        val spawnTile = capital.getCenterTile()
        val unit = civInfo.gameInfo.tileMap.placeUnitNearTile(spawnTile.position, heroName, civInfo)
        if (unit == null) return false

        hero.isRetired = false
        hero.cooldownTurns = 0
        hero.turnsUntilRetirement = heroRetirementTurns

        civInfo.addNotification(
            "Your Hero [$heroName] has returned!",
            spawnTile.position,
            NotificationCategory.Units,
            heroName
        )
        return true
    }

    /**
     * Process hero retirement countdown and cooldown each turn.
     */
    fun onTurnEnd() {
        for (hero in heroes.toList()) {
            if (hero.isRetired) {
                hero.cooldownTurns = max(0, hero.cooldownTurns - 1)
            } else {
                hero.turnsUntilRetirement--
                if (hero.turnsUntilRetirement <= 0) {
                    hero.isRetired = true
                    hero.cooldownTurns = heroReviveCooldown
                    civInfo.addNotification(
                        "Your Hero [${hero.heroName}] has retired after a long career!",
                        NotificationCategory.General,
                        "StatIcons/GreatPerson"
                    )
                }
            }
        }
    }
}
