package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.NotificationCategory
import com.unciv.logic.city.City
import yairm210.purity.annotations.Readonly
import kotlin.math.max

data class Corporation(
    val resourceName: String,
    val foundingCityId: String,
    val productName: String = ""
) : IsPartOfGameInfoSerialization

data class Monopoly(
    val resourceName: String,
    val totalWorldSupply: Int,
    val controlledSupply: Int,
    val monopolyPercentage: Float
) : IsPartOfGameInfoSerialization

class CorporationManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

    var corporations = mutableListOf<Corporation>()
    var monopolies = mutableListOf<Monopoly>()
    var industryResources = mutableSetOf<String>()
    var productsResearched = mutableListOf<String>()

    fun clone(): CorporationManager {
        val toReturn = CorporationManager()
        toReturn.corporations.addAll(corporations.map { it.copy() })
        toReturn.monopolies.addAll(monopolies.map { it.copy() })
        toReturn.industryResources.addAll(industryResources)
        toReturn.productsResearched.addAll(productsResearched)
        return toReturn
    }

    fun setTransients(civInfo: Civilization) {
        this.civInfo = civInfo
    }

    @Readonly
    fun hasMonopoly(resourceName: String): Boolean = monopolies.any { it.resourceName == resourceName }

    @Readonly
    fun hasCorporation(resourceName: String): Boolean = corporations.any { it.resourceName == resourceName }

    @Readonly
    fun getMonopolyBonus(resourceName: String): Float {
        val monopoly = monopolies.firstOrNull { it.resourceName == resourceName } ?: return 0f
        return when {
            monopoly.monopolyPercentage >= 0.75f -> 1.5f
            monopoly.monopolyPercentage >= 0.5f -> 1.0f
            else -> 0.5f
        }
    }

    @Readonly
    fun getCorporationBonus(resourceName: String): Float {
        if (!hasCorporation(resourceName)) return 0f
        return 2.0f
    }

    @Readonly
    fun calculateMonopolyPercentage(resourceName: String): Float {
        val totalWorld = getTotalWorldResource(resourceName)
        if (totalWorld <= 0) return 0f
        val controlled = civInfo.getResourceAmount(resourceName)
        return controlled.toFloat() / totalWorld.toFloat()
    }

    @Readonly
    private fun getTotalWorldResource(resourceName: String): Int {
        return civInfo.gameInfo.civilizations.sumOf { civ ->
            if (civ.isDefeated() || civ.isBarbarian) 0
            else civ.getResourceAmount(resourceName)
        }
    }

    fun checkAndEstablishMonopolies() {
        for (resource in civInfo.gameInfo.ruleset.tileResources.values) {
            val name = resource.name
            if (monopolies.any { it.resourceName == name }) continue

            val totalWorld = civInfo.gameInfo.getCivResourcesTotal(name)
            if (totalWorld <= 0) continue
            val controlled = civInfo.getResourceAmount(name)
            val percentage = controlled.toFloat() / totalWorld.toFloat()

            if (percentage >= 0.5f) {
                monopolies.add(Monopoly(name, totalWorld, controlled, percentage))
                civInfo.gameModes.establishMonopoly(name)
                industryResources.add(name)
                civInfo.addNotification(
                    "You have established a Monopoly on [$name]!",
                    NotificationCategory.General,
                    "StatIcons/GreatPerson"
                )
            }
        }
    }

    fun foundCorporation(city: City, resourceName: String) {
        if (!hasMonopoly(resourceName)) return
        if (hasCorporation(resourceName)) return

        val corporation = Corporation(resourceName, city.id)
        corporations.add(corporation)
        civInfo.gameModes.foundCorporation(resourceName)

        civInfo.addNotification(
            "You have founded a [$resourceName] Corporation in [${city.name}]!",
            city.location,
            NotificationCategory.General,
            "StatIcons/GreatPerson"
        )
    }

    fun researchProduct(productName: String) {
        if (productName in productsResearched) return
        productsResearched.add(productName)
        civInfo.addNotification(
            "You have researched the [$productName] product!",
            NotificationCategory.General,
            "StatIcons/GreatPerson"
        )
    }

    fun getCorporationYieldBonus(resourceName: String): Float {
        var bonus = 0f
        if (hasMonopoly(resourceName)) bonus += getMonopolyBonus(resourceName)
        if (hasCorporation(resourceName)) bonus += getCorporationBonus(resourceName)
        return bonus
    }

    fun onTurnEnd() {
        checkAndEstablishMonopolies()
    }
}
