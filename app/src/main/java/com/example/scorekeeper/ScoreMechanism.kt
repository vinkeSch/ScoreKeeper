package com.example.scorekeeper

import kotlin.math.abs

/**
 * Abstracts most of the Tennis scoring rules. Implementations of this interface
 * will be compute the current match/set computeSetScore based on the current rules being
 * played (Standard Tennis Rules or Tie Break).
 */
interface ScoreMechanism {

    val minScoreToWinGame: Int
    val playerA: Player
    val playerB: Player

    fun computeSetScore(): String {
        val matchScoreIsClose = abs(playerA.setScore - playerB.setScore ) < 2
        val setScoreDifference = playerA.gameScore - playerB.gameScore
        val currentSet = "${playerA.setScore}-${playerB.setScore}"

        return when {
            playerA.setScore == 7 -> "$currentSet, ${playerA.name} is the Winner"
            playerB.setScore == 7 -> "$currentSet, ${playerB.name} is the Winner"

            !matchScoreIsClose && playerA.setScore >= 6 -> "$currentSet, ${playerA.name} is the Winner"
            !matchScoreIsClose && playerB.setScore >= 6 -> "$currentSet, ${playerB.name} is the Winner"

            setScoreDifference ==  0 && playerA.gameScore == 0 -> currentSet

            else -> "$currentSet, ${computeGameScore()}"
        }
    }

    fun computeGameScore(): String
}