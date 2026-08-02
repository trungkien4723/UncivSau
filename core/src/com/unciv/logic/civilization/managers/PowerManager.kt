package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.NotificationCategory
import com.unciv.logic.city.City
import com.unciv.models.ruleset.unique.UniqueType
import yairm210.purity.annotations.Readonly
import kotlin.math.max

data class CityPowerStatus(
    val cityId: String,
    val production: Int,
    val consumption: Int,
    val isPowered: Boolean,
    val resourceType: String? = null
) : IsPartOfGameInfoSerialization

class PowerManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

    var totalCO2 = 0
    var powerProduction = 0
    var powerConsumption = 0
    var cityPowerStatuses = mutableListOf<CityPowerStatus>()

    fun clone(): PowerManager {
        val toReturn = PowerManager()
        toReturn.totalCO2 = totalCO2
        toReturn.powerProduction = powerProduction
        toReturn.powerConsumption = powerConsumption
        toReturn.cityPowerStatuses.addAll(cityPowerStatuses.map { it.copy() })
        return toReturn
    }

    fun setTransients(civInfo: Civilization) {
        this.civInfo = civInfo
    }

    fun addCO2(amount: Int) { totalCO2 += amount }
    fun reduceCO2(amount: Int) { totalCO2 = (totalCO2 - amount).coerceAtLeast(0) }

    fun calculatePower() {
        powerProduction = 0
        powerConsumption = 0
        totalCO2 = 0
        cityPowerStatuses.clear()

        for (city in civInfo.cities) {
            val cityProd = calculateCityProduction(city)
            val cityCons = calculateCityConsumption(city)

            cityPowerStatuses.add(CityPowerStatus(
                cityId = city.id,
                production = cityProd,
                consumption = cityCons,
                isPowered = cityProd >= cityCons,
                resourceType = getPowerResourceType(city)
            ))

            powerProduction += cityProd
            powerConsumption += cityCons
        }
    }

    private fun calculateCityProduction(city: City): Int {
        var prod = 0
        for (building in city.cityConstructions.getBuiltBuildings()) {
            for (unique in building.uniqueObjects) {
                if (unique.type == UniqueType.PowerProduction) {
                    val amount = unique.params[0].toInt()
                    val resource = getBuildingPowerResource(building.name)
                    if (resource == null || civInfo.getResourceAmount(resource) > 0) {
                        prod += amount
                        if (resource != null) {
                            val tileResource = civInfo.gameInfo.ruleset.tileResources[resource]
                            if (tileResource != null) civInfo.gainStockpiledResource(tileResource, -1)
                            totalCO2 += getCO2ForResource(resource)
                        }
                    }
                }
            }
        }
        return prod
    }

    private fun calculateCityConsumption(city: City): Int {
        var cons = 0
        for (building in city.cityConstructions.getBuiltBuildings()) {
            for (unique in building.uniqueObjects) {
                if (unique.type == UniqueType.PowerConsumption) {
                    cons += unique.params[0].toInt()
                } else if (unique.type == UniqueType.CO2Emission) {
                    totalCO2 += unique.params[0].toInt()
                }
            }
        }
        return cons
    }

    @Readonly
    private fun getBuildingPowerResource(buildingName: String): String? {
        val buildingInfo = civInfo.gameInfo.ruleset.buildings[buildingName] ?: return null
        // If the building directly produces power without needing a resource (e.g. Solar, Wind)
        for (unique in buildingInfo.uniqueObjects) {
            if (unique.type == UniqueType.PowerProduction && !unique.hasModifier(UniqueType.ConsumesResources)) {
                return null // No resource needed
            }
        }
        if (buildingName.contains("Coal")) return "Coal"
        if (buildingName.contains("Oil")) return "Oil"
        if (buildingName.contains("Nuclear")) return "Uranium"
        return null
    }

    @Readonly
    private fun getCO2ForResource(resource: String): Int = when (resource) {
        "Coal" -> 3
        "Oil" -> 2
        "Uranium" -> 0
        else -> 0
    }

    @Readonly
    private fun getPowerResourceType(city: City): String? {
        for (building in city.cityConstructions.getBuiltBuildings()) {
            if (building.name.contains("Coal")) return "Coal"
            if (building.name.contains("Oil")) return "Oil"
            if (building.name.contains("Nuclear")) return "Uranium"
            if (building.name.contains("Solar")) return "Solar"
            if (building.name.contains("Wind")) return "Wind"
            if (building.name.contains("Hydro")) return "Hydro"
        }
        return null
    }

    @Readonly
    fun isCityPowered(city: City): Boolean {
        return cityPowerStatuses.any { it.cityId == city.id && it.isPowered }
    }

    @Readonly
    fun getPowerConsumption(buildingName: String): Int {
        val buildingInfo = civInfo.gameInfo.ruleset.buildings[buildingName] ?: return 0
        for (unique in buildingInfo.uniqueObjects) {
            if (unique.type == UniqueType.PowerConsumption && unique.params.size >= 1) {
                return unique.params[0].toInt()
            }
        }
        return 0
    }

    @Readonly
    fun getPowerProduction(buildingName: String): Int {
        val buildingInfo = civInfo.gameInfo.ruleset.buildings[buildingName] ?: return 0
        for (unique in buildingInfo.uniqueObjects) {
            if (unique.type == UniqueType.PowerProduction && unique.params.size >= 1) {
                return unique.params[0].toInt()
            }
        }
        return 0
    }

    @Readonly
    fun getPowerDeficit(): Int = max(0, powerConsumption - powerProduction)
    @Readonly
    fun isPowerDeficit(): Boolean = powerConsumption > powerProduction
    @Readonly
    fun getCO2Level(): Int = totalCO2

    @Readonly
    fun getPoweredCityCount(): Int = cityPowerStatuses.count { it.isPowered }
    @Readonly
    fun getUnpoweredCityCount(): Int = cityPowerStatuses.count { !it.isPowered }
}
