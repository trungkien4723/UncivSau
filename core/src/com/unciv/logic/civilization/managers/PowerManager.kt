package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import com.unciv.models.ruleset.unique.UniqueType
import yairm210.purity.annotations.Readonly
import kotlin.math.max

class PowerManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

    var totalCO2 = 0
    var powerProduction = 0
    var powerConsumption = 0

    fun clone(): PowerManager {
        val toReturn = PowerManager()
        toReturn.totalCO2 = totalCO2
        toReturn.powerProduction = powerProduction
        toReturn.powerConsumption = powerConsumption
        return toReturn
    }

    fun addCO2(amount: Int) {
        totalCO2 += amount
    }

    fun reduceCO2(amount: Int) {
        totalCO2 = (totalCO2 - amount).coerceAtLeast(0)
    }

    fun calculatePower() {
        powerProduction = 0
        powerConsumption = 0
        totalCO2 = 0 // Reset and recalculate CO2
        for (city in civInfo.cities) {
            for (building in city.cityConstructions.getBuiltBuildings()) {
                for (unique in building.uniqueObjects) {
                    if (unique.type == UniqueType.PowerProduction) {
                        powerProduction += unique.params[1].toInt()
                    } else if (unique.type == UniqueType.PowerConsumption) {
                        powerConsumption += unique.params[1].toInt()
                    } else if (unique.type == UniqueType.CO2Emission) {
                        totalCO2 += unique.params[1].toInt()
                    }
                }
            }
        }
    }

    @Readonly
    fun getPowerConsumption(buildingName: String): Int {
        val buildingInfo = civInfo.gameInfo.ruleset.buildings[buildingName] ?: return 0
        for (unique in buildingInfo.uniques) {
            if (unique.startsWith("PowerConsumption:")) {
                return unique.substringAfter(":").toInt()
            }
        }
        return 0
    }

    @Readonly
    fun getPowerProduction(buildingName: String): Int {
        val buildingInfo = civInfo.gameInfo.ruleset.buildings[buildingName] ?: return 0
        for (unique in buildingInfo.uniques) {
            if (unique.startsWith("PowerProduction:")) {
                return unique.substringAfter(":").toInt()
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
}