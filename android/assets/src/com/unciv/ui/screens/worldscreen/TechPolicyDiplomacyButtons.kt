package com.unciv.ui.screens.worldscreen

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.unciv.models.UncivSound
import com.unciv.models.translations.tr
import com.unciv.ui.components.extensions.colorFromRGB
import com.unciv.ui.components.extensions.disable
import com.unciv.ui.components.extensions.setFontSize
import com.unciv.ui.components.extensions.toLabel
import com.unciv.ui.components.extensions.toTextButton
import com.unciv.ui.components.fonts.Fonts
import com.unciv.ui.components.input.KeyboardBinding
import com.unciv.ui.components.input.onActivation
import com.unciv.ui.images.ImageGetter
import com.unciv.ui.screens.basescreen.BaseScreen
import com.unciv.ui.screens.diplomacyscreen.DiplomacyScreen
import com.unciv.ui.screens.overviewscreen.EspionageOverviewScreen
import com.unciv.ui.screens.pickerscreens.CivicButton
import com.unciv.ui.screens.pickerscreens.CivicPickerScreen
import com.unciv.ui.screens.pickerscreens.GovernmentPickerScreen
import com.unciv.ui.screens.pickerscreens.PolicyPickerScreen
import com.unciv.ui.screens.pickerscreens.TechButton
import com.unciv.ui.screens.pickerscreens.TechPickerScreen
import com.unciv.ui.screens.worldscreen.UndoHandler.Companion.canUndo
import com.unciv.ui.screens.worldscreen.UndoHandler.Companion.restoreUndoCheckpoint


/** A holder for Tech, Policies and Diplomacy buttons going in the top left of the WorldScreen just under WorldScreenTopBar */
class TechPolicyDiplomacyButtons(val worldScreen: WorldScreen) : Table(BaseScreen.skin) {
    private val fogOfWarButtonHolder = Container<Button?>()
    private val fogOfWarButton = "Fog of War".toTextButton()

    private val techButtonHolder = Container<Table?>()
    private val pickTechButton = Table(skin)
    private val pickTechLabel = "".toLabel(Color.WHITE, 30)

    private val civicButtonHolder = Container<Table?>()
    private val pickCivicButton = Table(skin)
    private val pickCivicLabel = "".toLabel(Color.WHITE, 30)
    private val pickCivicInnerLabel = "".toLabel(Color.WHITE, 30)

    private val governmentButtonHolder = Container<Button?>()
    private val governmentScreenButton = Button(skin)

    private val policyButtonHolder = Container<Button?>()
    private val policyScreenButton = Button(skin)
    private val diplomacyButtonHolder = Container<Button?>()
    private val diplomacyButton = Button(skin)
    private val undoButtonHolder = Container<Button?>()
    private val undoButton = Button(skin)
    private val espionageButtonHolder = Container<Button?>()
    private val espionageButton = Button(skin)

    private val viewingCiv = worldScreen.viewingCiv
    private val game = worldScreen.game

    /** Whether the current ruleset uses the Civ VI government system (Governments.json present). */
    private var hasGovernmentButton = false

    init {
        defaults().left()
        add(fogOfWarButtonHolder).colspan(4).row()
        add(techButtonHolder).colspan(4).row()
        add(civicButtonHolder).colspan(4).row()
        hasGovernmentButton = worldScreen.gameInfo.ruleset.governments.isNotEmpty()
        if (hasGovernmentButton) add(governmentButtonHolder).colspan(4).row()
        add(policyButtonHolder).padTop(10f).padRight(10f)
        add(diplomacyButtonHolder).padTop(10f).padRight(10f)
        add(espionageButtonHolder).padTop(10f).padRight(10f)
        add(undoButtonHolder).padTop(10f).padRight(10f)
        add().growX()  // Allows Policy and Diplo buttons to keep to the left

        fogOfWarButton.label.setFontSize(30)
        fogOfWarButton.labelCell.pad(10f)
        fogOfWarButton.pack()
        fogOfWarButtonHolder.onActivation(UncivSound.Paper, KeyboardBinding.TechnologyTree) {
            worldScreen.fogOfWar = !worldScreen.fogOfWar
            worldScreen.shouldUpdate = true
        }

        pickTechButton.background = BaseScreen.skinStrings.getUiBackground("WorldScreen/PickTechButton", BaseScreen.skinStrings.roundedEdgeRectangleShape, colorFromRGB(7, 46, 43))
        pickTechButton.defaults().pad(20f)
        pickTechButton.add(pickTechLabel)
        techButtonHolder.onActivation(UncivSound.Paper, KeyboardBinding.TechnologyTree) {
            game.pushScreen(TechPickerScreen(viewingCiv))
        }

        pickCivicButton.background = BaseScreen.skinStrings.getUiBackground("WorldScreen/PickCivicButton", BaseScreen.skinStrings.roundedEdgeRectangleShape, colorFromRGB(46, 7, 43))
        pickCivicButton.defaults().pad(20f)
        pickCivicButton.add(pickCivicLabel)
        civicButtonHolder.onActivation(UncivSound.Paper, KeyboardBinding.CivicTree) {
            game.pushScreen(CivicPickerScreen(viewingCiv))
        }

        governmentScreenButton.add(ImageGetter.getImage("OtherIcons/Government")).size(30f).pad(15f)
        governmentButtonHolder.onActivation {
            game.pushScreen(GovernmentPickerScreen(viewingCiv))
        }

        undoButton.add(ImageGetter.getImage("OtherIcons/Undo")).size(30f).pad(15f)
        undoButton.onActivation(binding = KeyboardBinding.Undo) {
            handleUndo()
        }

        policyScreenButton.add(ImageGetter.getImage("OtherIcons/Policies")).size(30f).pad(15f)
        policyButtonHolder.onActivation(binding = KeyboardBinding.SocialPolicies) {
            game.pushScreen(PolicyPickerScreen(worldScreen.selectedCiv, worldScreen.canChangeState))
        }

        diplomacyButton.add(ImageGetter.getImage("OtherIcons/DiplomacyW")).size(30f).pad(15f)
        diplomacyButtonHolder.onActivation(binding = KeyboardBinding.Diplomacy) {
            game.pushScreen(DiplomacyScreen(viewingCiv))
        }

        if (game.gameInfo!!.isEspionageEnabled()) {
            espionageButton.add(ImageGetter.getImage("OtherIcons/Espionage")).size(30f).pad(15f)
            espionageButtonHolder.onActivation(binding = KeyboardBinding.Espionage) {
                // We want to make sure to deselect a spy in the case that the player wants to cancel moving
                // the spy on the map screen by pressing this button
                if (worldScreen.bottomUnitTable.selectedSpy != null) {
                    worldScreen.bottomUnitTable.selectSpy(null)
                }
                game.pushScreen(EspionageOverviewScreen(worldScreen.selectedCiv, worldScreen))
            }
        }
    }

    fun update(): Boolean {
        updateFogOfWarButton()
        updateTechButton()
        updateCivicButton()
        updateGovernmentButton()
        updateUndoButton()
        updatePolicyButton()
        val result = updateDiplomacyButton()
        if (game.gameInfo!!.isEspionageEnabled())
            updateEspionageButton()
        pack()
        setPosition(10f, worldScreen.topBar.y - height - 15f)
        return result
    }

    private fun updateFogOfWarButton() {
        if (viewingCiv.isSpectator()) {
            fogOfWarButtonHolder.actor = fogOfWarButton
            fogOfWarButtonHolder.touchable = Touchable.enabled
        } else {
            fogOfWarButtonHolder.touchable = Touchable.disabled
            fogOfWarButtonHolder.actor = null
        }
    }

    private fun updateTechButton() {
        techButtonHolder.touchable = Touchable.disabled
        techButtonHolder.actor = null
        if (worldScreen.gameInfo.ruleset.technologies.isEmpty() || viewingCiv.cities.isEmpty()) return
        techButtonHolder.touchable = Touchable.enabled

        if (viewingCiv.tech.currentTechnology() != null) {
            val currentTech = viewingCiv.tech.currentTechnologyName()!!
            val innerButton = TechButton(currentTech, viewingCiv.tech)
            innerButton.setButtonColor(colorFromRGB(7, 46, 43))
            techButtonHolder.actor = innerButton
            val turnsToTech = viewingCiv.tech.turnsToTech(currentTech)
            innerButton.text.setText(currentTech.tr(true))
            innerButton.turns.setText(turnsToTech + Fonts.turn)
        } else {
            val canResearch = viewingCiv.tech.canResearchTech()
            if (canResearch || viewingCiv.tech.researchedTechnologies.size != 0) {
                val text = if (canResearch) "{Pick a tech}!" else "Technologies"
                pickTechLabel.setText(text.tr())
                techButtonHolder.actor = pickTechButton
            }
        }
    }

    private fun updateCivicButton() {
        civicButtonHolder.touchable = Touchable.disabled
        civicButtonHolder.actor = null
        if (worldScreen.gameInfo.ruleset.civics.isEmpty() || viewingCiv.cities.isEmpty()) return
        civicButtonHolder.touchable = Touchable.enabled

        if (viewingCiv.civics.currentCivic() != null) {
            val currentCivic = viewingCiv.civics.currentCivicName()!!
            val innerButton = CivicButton(currentCivic, viewingCiv.civics)
            innerButton.setButtonColor(colorFromRGB(46, 7, 43))
            civicButtonHolder.actor = innerButton
            val turnsToCivic = viewingCiv.civics.turnsToCivic(currentCivic)
            innerButton.text.setText(currentCivic.tr(true))
            innerButton.turns.setText(turnsToCivic + Fonts.turn)
        } else {
            val canResearch = viewingCiv.civics.canResearchCivic()
            if (canResearch || viewingCiv.civics.researchedCivics.size != 0) {
                val text = if (canResearch) "{Pick a civic}!" else "Civics"
                pickCivicLabel.setText(text.tr())
                civicButtonHolder.actor = pickCivicButton
            }
        }
    }

    private fun updateGovernmentButton() {
        if (!hasGovernmentButton) {
            governmentButtonHolder.touchable = Touchable.disabled
            governmentButtonHolder.actor = null
            return
        }
        governmentButtonHolder.touchable = Touchable.enabled
        governmentButtonHolder.actor = governmentScreenButton
    }

    private fun updateUndoButton() {
        // Don't show the undo button if there is no action to undo
        if (worldScreen.canUndo()) {
            undoButtonHolder.touchable = Touchable.enabled
            undoButtonHolder.actor = undoButton
        } else {
            undoButtonHolder.touchable = Touchable.disabled
            undoButtonHolder.actor = null
        }
    }

    private fun updatePolicyButton() {
        // Don't show policies until they become relevant
        if (viewingCiv.policies.adoptedPolicies.isNotEmpty() || viewingCiv.policies.canAdoptPolicy()) {
            policyButtonHolder.touchable = Touchable.enabled
            policyButtonHolder.actor = policyScreenButton
        } else {
            policyButtonHolder.touchable = Touchable.disabled
            policyButtonHolder.actor = null
        }
    }

    private fun updateDiplomacyButton(): Boolean {
        return if (viewingCiv.isDefeated() || viewingCiv.isSpectator()
                || viewingCiv.getKnownCivs().filterNot { it == viewingCiv || it.isBarbarian }.none()
        ) {
            diplomacyButtonHolder.touchable = Touchable.disabled
            diplomacyButtonHolder.actor = null
            false
        } else {
            diplomacyButtonHolder.touchable = Touchable.enabled
            diplomacyButtonHolder.actor = diplomacyButton
            true
        }
    }

    private fun updateEspionageButton() {
        if (worldScreen.selectedCiv.espionageManager.spyList.isEmpty()) {
            espionageButtonHolder.touchable = Touchable.disabled
            espionageButtonHolder.actor = null
        } else {
            espionageButtonHolder.touchable = Touchable.enabled
            espionageButtonHolder.actor = espionageButton
        }
    }

    private fun handleUndo() {
        undoButton.disable()
        worldScreen.restoreUndoCheckpoint()
    }

    override fun act(delta: Float) = super.act(delta)
    override fun draw(batch: Batch?, parentAlpha: Float) = super.draw(batch, parentAlpha)
    override fun hit(x: Float, y: Float, touchable: Boolean): Actor? = super.hit(x, y, touchable)
}
