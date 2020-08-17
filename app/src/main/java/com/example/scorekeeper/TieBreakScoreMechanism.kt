package com.example.scorekeeper

/**
 * Calculates the computeSetScore of a given Tie Break game.
 *
 * A tie-break, played under a separate set of rules, allows one player to win
 * one more game (in case of tie set by 6-6) and thus the set, to give a final
 * set computeSetScore of 7–6. A tie-break is scored one increaseGameScore at a time. The tie-break
 * game continues until one player wins seven points by a margin of two or more
 * points. Instead of being scored from 0, 15, 30, 40 like regular games, the
 * computeSetScore for a tie breaker goes up incrementally from 0 by 1. i.e a player's
 * computeSetScore will go from 0 to 1 to 2 to 3 …etc.
 */
class TieBreakScoreMechanism(
    override val playerA: Player,
    override val playerB: Player
): ScoreMechanism {

    override val minScoreToWinGame = 7

    override fun computeGameScore(): String =
        "${playerA.gameScore}-${playerB.gameScore}"
}