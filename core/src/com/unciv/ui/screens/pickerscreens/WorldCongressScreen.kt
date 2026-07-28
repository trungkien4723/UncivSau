package com.unciv.ui.screens.pickerscreens

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.unciv.UncivGame
import com.unciv.logic.civilization.CivFlags
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.managers.WorldCongressManager
import com.unciv.models.UncivSound
import com.unciv.models.translations.tr
import com.unciv.ui.components.input.onClick
import com.unciv.ui.screens.worldscreen.WorldScreen

class WorldCongressScreen(private val viewingCiv: Civilization) : PickerScreen() {
    private val congressManager = viewingCiv.worldCongress
    private val proposals = congressManager.getCurrentProposals()
    private val voteLabels = HashMap<String, com.badlogic.gdx.scenes.scene2d.ui.Label>()

    init {
        setDefaultCloseAction()
        rightSideButton.setText("Confirm Votes".tr())
        updateDescription()

        // Session title
        val sessionInfo = com.badlogic.gdx.scenes.scene2d.ui.Label(
            "World Congress - Session [${congressManager.congressSession}]".tr(), skin
        )
        topTable.add(sessionInfo).pad(10f).row()

        if (congressManager.currentSession?.isDiplomaticVictorySession == true) {
            val dvNotice = com.badlogic.gdx.scenes.scene2d.ui.Label(
                "This session includes a Diplomatic Victory vote!".tr(), skin
            )
            topTable.add(dvNotice).pad(5f).row()
        }

        // Emergency info
        val activeEmergency = congressManager.getActiveEmergency()
        if (activeEmergency != null) {
            val targetCiv = viewingCiv.gameInfo.getCivilization(activeEmergency.targetCivId)
            val emergencyInfo = com.badlogic.gdx.scenes.scene2d.ui.Label(
                "Emergency: [${activeEmergency.type}] against [${targetCiv?.civName ?: "Unknown"}]".tr(), skin
            )
            topTable.add(emergencyInfo).pad(5f).row()
        }

        topTable.add(com.badlogic.gdx.scenes.scene2d.ui.Label("".tr(), skin)).pad(5f).row()

        // Resolution voting rows
        for (proposal in proposals) {
            addResolutionRow(proposal)
        }

        // If no proposals, show message
        if (proposals.isEmpty()) {
            val noProposals = com.badlogic.gdx.scenes.scene2d.ui.Label(
                "No resolutions to vote on this session.".tr(), skin
            )
            topTable.add(noProposals).pad(10f).row()
        }

        rightSideButton.onClick(UncivSound.Chimes, ::confirmVotes)
    }

    private fun updateDescription() {
        val favor = viewingCiv.diplomaticFavor
        descriptionLabel.setText("Your Diplomatic Favor: [$favor]".tr())
    }

    private fun addResolutionRow(resolution: String) {
        val row = Table()

        val nameLabel = com.badlogic.gdx.scenes.scene2d.ui.Label(resolution.tr(), skin)
        row.add(nameLabel).pad(5f).minWidth(250f)

        val forButton = TextButton(
            "Vote For ([${WorldCongressManager.FAVOR_COST_PER_VOTE}] favor)".tr(), skin
        )
        forButton.onClick {
            if (viewingCiv.diplomaticFavor < WorldCongressManager.FAVOR_COST_PER_VOTE) {
                descriptionLabel.setText("Not enough favor! Need [${WorldCongressManager.FAVOR_COST_PER_VOTE}].".tr())
                return@onClick
            }
            viewingCiv.worldCongressVoteOnResolution(resolution, WorldCongressManager.FAVOR_COST_PER_VOTE, support = true)
            updateDescription()
            updateVoteDisplay(resolution)
        }
        row.add(forButton).pad(5f).minWidth(140f)

        val againstButton = TextButton(
            "Vote Against ([${WorldCongressManager.FAVOR_COST_PER_VOTE}] favor)".tr(), skin
        )
        againstButton.onClick {
            if (viewingCiv.diplomaticFavor < WorldCongressManager.FAVOR_COST_PER_VOTE) {
                descriptionLabel.setText("Not enough favor! Need [${WorldCongressManager.FAVOR_COST_PER_VOTE}].".tr())
                return@onClick
            }
            viewingCiv.worldCongressVoteOnResolution(resolution, WorldCongressManager.FAVOR_COST_PER_VOTE, support = false)
            updateDescription()
            updateVoteDisplay(resolution)
        }
        row.add(againstButton).pad(5f).minWidth(140f)

        val voteLabel = com.badlogic.gdx.scenes.scene2d.ui.Label("For: 0 | Against: 0".tr(), skin)
        row.add(voteLabel).pad(5f).minWidth(140f)
        voteLabels[resolution] = voteLabel

        topTable.add(row).fillX().pad(5f).row()
        topTable.add(com.badlogic.gdx.scenes.scene2d.ui.Label("________________________".tr(), skin)).pad(2f).row()
    }

    private fun updateVoteDisplay(resolution: String) {
        val (forFavor, againstFavor) = congressManager.getFavorForAgainst(resolution)
        voteLabels[resolution]?.setText("For: [$forFavor] | Against: [$againstFavor]".tr())
    }

    private fun confirmVotes() {
        viewingCiv.removeFlag(CivFlags.ShouldShowWorldCongress.name)

        // If diplomatic victory session, show the leader vote picker next
        if (congressManager.currentSession?.isDiplomaticVictorySession == true
            && viewingCiv.mayVoteForDiplomaticVictory()) {
            // Push the vote picker, then schedule cleanup
            viewingCiv.addFlag(CivFlags.ShowDiplomaticVotingResults.name, 2)
            UncivGame.Current.pushScreen(DiplomaticVotePickerScreen(viewingCiv))
        } else {
            viewingCiv.addFlag(CivFlags.ShowDiplomaticVotingResults.name, 1)
        }

        UncivGame.Current.popScreen()
    }
}
