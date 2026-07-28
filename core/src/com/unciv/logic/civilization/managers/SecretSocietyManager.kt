package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.city.City
import com.unciv.logic.civilization.NotificationCategory
import com.unciv.models.Counter
import yairm210.purity.annotations.Readonly

/** Civ VI Secret Societies (Ethiopia Pack / Secret Societies Game Mode) */
enum class SecretSociety {
    NONE,
    HERMETIC_ORDER,     // Ley Lines - science/culture from tiles
    OWLS_OF_MINERVA,    // Economic - trade routes, envoys, gold
    SANGUINE_PACT,      // Vampires - combat units, governor manipulation
    VOID_SINGERS        // Faith/culture, relics, old god obelisks
}

enum class SocietyRank {
    UNASSIGNED,     // Not a member
    INITIATE,       // Rank 1 - basic bonuses
    ADEPT,          // Rank 2 - enhanced bonuses
    MASTER          // Rank 3 - powerful unique abilities
}

class SecretSocietyManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

    var chosenSociety: SecretSociety = SecretSociety.NONE
    var currentRank: SocietyRank = SocietyRank.UNASSIGNED
    var societyXP: Int = 0
    var societyFavor: Int = 0  // Separate from Diplomatic Favor

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
        
        applySocietyBonuses()
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
            val oldRank = currentRank
            currentRank = newRank
            civInfo.addNotification("You have advanced to ${newRank.name} in the ${getSocietyName()}!",
                NotificationCategory.General, "StatIcons/GreatPerson")
            applySocietyBonuses()
        }
    }

    fun applySocietyBonuses() {
        when (chosenSociety) {
            SecretSociety.HERMETIC_ORDER -> applyHermeticOrderBonuses()
            SecretSociety.OWLS_OF_MINERVA -> applyOwlsOfMinervaBonuses()
            SecretSociety.SANGUINE_PACT -> applySanguinePactBonuses()
            SecretSociety.VOID_SINGERS -> applyVoidSingersBonuses()
            else -> {} // NONE or unassigned
        }
    }

    private fun applyHermeticOrderBonuses() {
        // Ley Lines: Science and Culture from adjacent districts
        for (city in civInfo.cities) {
            // Ley Line mechanics: science/culture from tile improvements
        }
    }

    private fun applyOwlsOfMinervaBonuses() {
        // Trade route capacity, envoy bonuses
        for (city in civInfo.cities) {
            // Trade route and envoy bonuses
        }
    }

    private fun applySanguinePactBonuses() {
        // Vampire units, governor manipulation
    }

    private fun applyVoidSingersBonuses() {
        // Faith/culture from relics, old god obelisks
    }

    fun onTurnEnd() {
        // Passive society XP gain
        if (isMember()) {
            societyXP += 1
            checkRankUp()
        }
    }
}