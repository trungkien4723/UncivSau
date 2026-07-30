package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.city.City
import com.unciv.logic.civilization.NotificationCategory
import com.unciv.models.Counter
import com.unciv.models.ruleset.unique.Unique
import com.unciv.models.ruleset.unique.UniqueMap
import com.unciv.models.ruleset.unique.UniqueType
import com.unciv.models.ruleset.unique.GameContext
import yairm210.purity.annotations.Readonly

enum class SecretSociety {
    NONE,
    HERMETIC_ORDER,
    OWLS_OF_MINERVA,
    SANGUINE_PACT,
    VOID_SINGERS
}

enum class SocietyRank {
    UNASSIGNED,
    INITIATE,
    ADEPT,
    MASTER
}

class SecretSocietyManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

    var chosenSociety: SecretSociety = SecretSociety.NONE
    var currentRank: SocietyRank = SocietyRank.UNASSIGNED
    var societyXP: Int = 0
    var societyFavor: Int = 0

    @Transient
    private var cachedUniques: List<String> = emptyList()

    @Transient
    private var cachedUniqueMap = UniqueMap()

    fun clone(): SecretSocietyManager {
        val toReturn = SecretSocietyManager()
        toReturn.chosenSociety = chosenSociety
        toReturn.currentRank = currentRank
        toReturn.societyXP = societyXP
        toReturn.societyFavor = societyFavor
        return toReturn
    }

    fun setTransients(civ: Civilization) {
        civInfo = civ
        rebuildCache()
    }

    private fun rebuildCache() {
        cachedUniques = generateSocietyUniques()
        cachedUniqueMap = UniqueMap()
        for (uniqueText in cachedUniques) {
            cachedUniqueMap.addUnique(Unique(uniqueText))
        }
    }

    @Readonly
    fun isMember(): Boolean = chosenSociety != SecretSociety.NONE

    @Readonly
    fun getSocietyName(): String = when (chosenSociety) {
        SecretSociety.HERMETIC_ORDER -> "Hermetic Order"
        SecretSociety.OWLS_OF_MINERVA -> "Owls of Minerva"
        SecretSociety.SANGUINE_PACT -> "Sanguine Pact"
        SecretSociety.VOID_SINGERS -> "Void Singers"
        else -> "None"
    }

    @Readonly
    fun getRankName(): String = when (currentRank) {
        SocietyRank.UNASSIGNED -> "Unaffiliated"
        SocietyRank.INITIATE -> "Initiate"
        SocietyRank.ADEPT -> "Adept"
        SocietyRank.MASTER -> "Master"
    }

    fun joinSociety(society: SecretSociety) {
        if (isMember()) return
        chosenSociety = society
        currentRank = SocietyRank.INITIATE
        societyXP = 0
        societyFavor = 0

        civInfo.addNotification("You have joined the ${getSocietyName()}!",
            NotificationCategory.Diplomacy, "StatIcons/GreatPerson")

        rebuildCache()
    }

    fun addSocietyXP(amount: Int) {
        if (!isMember()) return
        societyXP += amount
        checkRankUp()
    }

    fun addSocietyFavor(amount: Int) {
        if (!isMember()) return
        societyFavor += amount
    }

    private fun checkRankUp() {
        val newRank = when {
            societyXP >= 100 -> SocietyRank.MASTER
            societyXP >= 30 -> SocietyRank.ADEPT
            societyXP >= 5 -> SocietyRank.INITIATE
            else -> SocietyRank.UNASSIGNED
        }

        if (newRank != currentRank && newRank != SocietyRank.UNASSIGNED) {
            currentRank = newRank
            civInfo.addNotification("You have advanced to ${newRank.name} in the ${getSocietyName()}!",
                NotificationCategory.General, "StatIcons/GreatPerson")
            rebuildCache()
        }
    }

    @Readonly
    private fun generateSocietyUniques(): List<String> {
        if (!isMember()) return emptyList()

        return when (chosenSociety) {
            SecretSociety.HERMETIC_ORDER -> {
                val list = mutableListOf<String>()
                when (currentRank) {
                    SocietyRank.INITIATE -> {
                        list.add("[+2] [Science] [in all cities]")
                        list.add("[+1] [Culture] [in all cities]")
                    }
                    SocietyRank.ADEPT -> {
                        list.add("[+4] [Science] [in all cities]")
                        list.add("[+2] [Culture] [in all cities]")
                    }
                    SocietyRank.MASTER -> {
                        list.add("[+6] [Science] [in all cities]")
                        list.add("[+4] [Culture] [in all cities]")
                        list.add("[+2] [Science] from [Great Scientist] units")
                    }
                    else -> {}
                }
                list
            }
            SecretSociety.OWLS_OF_MINERVA -> {
                val list = mutableListOf<String>()
                when (currentRank) {
                    SocietyRank.INITIATE -> list.add("[+1] Trade Route capacity")
                    SocietyRank.ADEPT -> {
                        list.add("[+2] Trade Route capacity")
                        list.add("[+2] [Gold] from [Trade Routes]")
                    }
                    SocietyRank.MASTER -> {
                        list.add("[+4] Trade Route capacity")
                        list.add("[+5] [Gold] from [International] Trade Routes")
                    }
                    else -> {}
                }
                list
            }
            SecretSociety.SANGUINE_PACT -> {
                val list = mutableListOf<String>()
                when (currentRank) {
                    SocietyRank.INITIATE -> list.add("[+10]% Strength <for [All] units>")
                    SocietyRank.ADEPT -> {
                        list.add("[+15]% Strength <for [All] units>")
                        list.add("[+1] Movement <for [All] units>")
                    }
                    SocietyRank.MASTER -> {
                        list.add("[+20]% Strength <for [All] units>")
                        list.add("[+1] Movement <for [All] units>")
                    }
                    else -> {}
                }
                list
            }
            SecretSociety.VOID_SINGERS -> {
                val list = mutableListOf<String>()
                when (currentRank) {
                    SocietyRank.INITIATE -> {
                        list.add("[+4] [Faith] from [Monument] buildings")
                        list.add("[+2] [Culture] from [Monument] buildings")
                    }
                    SocietyRank.ADEPT -> {
                        list.add("[+8] [Faith] from [Monument] buildings")
                        list.add("[+4] [Culture] from [Monument] buildings")
                        list.add("[+2] [Faith] from [Relics]")
                        list.add("[+2] [Culture] from [Relics]")
                    }
                    SocietyRank.MASTER -> {
                        list.add("[+12] [Faith] from [Monument] buildings")
                        list.add("[+6] [Culture] from [Monument] buildings")
                        list.add("[+4] [Faith] from [Great Work of Writing]")
                    }
                    else -> {}
                }
                list
            }
            else -> emptyList()
        }
    }

    @Readonly
    fun getMatchingUniques(uniqueType: UniqueType, gameContext: GameContext) =
        cachedUniqueMap.getMatchingUniques(uniqueType, gameContext)

    @Readonly
    fun forEachMatchingUnique(uniqueType: UniqueType, gameContext: GameContext, op: (unique: Unique) -> Unit) {
        cachedUniqueMap.forEachMatchingUnique(uniqueType, gameContext, op)
    }

    fun onTurnEnd() {
        if (isMember()) {
            societyXP += 1
            checkRankUp()
        }
    }
}
