package com.unciv.civ6.combat

import com.unciv.civ6.domain.combat.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CombatTest {
    @Test
    fun siegeNoPenaltyVsCity() {
        val siege = UnitCombatant("Catapult", baseStrength = 35, isSiege = true, isRanged = true, rangedStrength = 35)
        val city = CityCombatant("City", baseStrength = 30, wallHp = 100)
        // Siege should not have -17 vs city, so strength 35 vs 30 = +5 diff
        assertEquals(35, siege.strengthAgainst(city))
        val melee = UnitCombatant("Warrior", baseStrength = 20)
        assertEquals(20, melee.strengthAgainst(city))
    }

    @Test
    fun siegePenaltyVsUnit() {
        val siege = UnitCombatant("Catapult", baseStrength = 20, isSiege = true)
        val warrior = UnitCombatant("Warrior", baseStrength = 20)
        assertEquals(3, siege.strengthAgainst(warrior)) // 20-17
    }

    @Test
    fun wallsOnlyHpNotStrength() {
        val cityNoWall = CityCombatant("City", baseStrength = 30, wallHp = 0)
        val cityWall = CityCombatant("City", baseStrength = 30, wallHp = 200)
        // Strength same regardless of wall HP (fix 4.26.5)
        assertEquals(cityNoWall.baseStrength, cityWall.baseStrength)
        assertEquals(30, cityWall.strengthAgainst(UnitCombatant("Warrior", 20)))
    }

    @Test
    fun battleDamageWallsAbsorb() {
        val catapult = UnitCombatant("Catapult", baseStrength = 35, isSiege = true, isRanged = true, rangedStrength = 35, currentHp = 100)
        val city = CityCombatant("City", baseStrength = 20, wallHp = 100, currentHp = 100)
        val result = Battle.siegeAttack(catapult, city)
        // Wall should take damage first
        assertTrue(result.wallHpAfter < 100)
        assertEquals(100, result.defenderHpAfter) // city HP untouched while walls up (if wall not destroyed)
    }

    @Test
    fun meleeRetaliation() {
        val warrior = UnitCombatant("Warrior", baseStrength = 20, currentHp = 100)
        val spear = UnitCombatant("Spearman", baseStrength = 25, currentHp = 100)
        val dmg = BattleDamage.calculateDamage(warrior, spear)
        assertTrue(dmg.damageToAttacker > 0) // melee retaliation
        val archer = UnitCombatant("Archer", baseStrength = 10, isRanged = true, rangedStrength = 25, currentHp = 100)
        val dmgRanged = BattleDamage.calculateDamage(archer, warrior)
        assertEquals(0, dmgRanged.damageToAttacker) // ranged no retaliation
    }

    @Test
    fun terrainAndSupportBonus() {
        assertEquals(3, BattleDamage.terrainModifier(true, false, false)) // hill
        assertEquals(6, BattleDamage.terrainModifier(false, false, true)) // fortified
        assertEquals(4, BattleDamage.supportBonus(2)) // 2 allies
        assertEquals(6, BattleDamage.supportBonus(3))
    }

    @Test
    fun equalStrengthDamage() {
        val a = UnitCombatant("A", 30, currentHp = 100)
        val b = UnitCombatant("B", 30, currentHp = 100)
        val dmg = BattleDamage.calculateDamage(a, b)
        assertEquals(30, dmg.damageToDefender) // 30 * exp(0) =30
    }
}
