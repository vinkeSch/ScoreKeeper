package com.example.scorekeeper

/**
 * Represents the score mechanism used during most of games
 * of a Tennis set.
 */
class StandardScoreMechanism(
    override val playerA: Player,
    override val playerB: Player
): ScoreMechanism {

    val scores = listOf("0", "15", "30", "40")

    override val minScoreToWinGame = 4

    /**
     * Computes the computeSetScore of a given set. It should consider the following rules:
     *  - A game is won by the first player to have won at least 4 points in total
     *    and at least 2 points more than the opponent
     *  - If at least 3 points have been scored by each player, and the scores
     *    are equal, the computeSetScore is "deuce"
     *  - If at least 3 points have been scored by each side and a player has one
     *    more increaseGameScore than his opponent, the computeSetScore of the game is "advantage" for
     *    the player in the lead.
     */
    override fun computeGameScore(): String {
        val setScoreDifference = playerA.gameScore - playerB.gameScore

        return when {
            setScoreDifference ==  0 && playerA.gameScore >= 3 -> {playerA.deucePlayer()
                playerB.deucePlayer()
                "Deuce"
            }
            setScoreDifference == -1 && playerA.gameScore >= 3 -> {playerB.advPlayer()
                "Advantage ${playerB.name}"
            }
            setScoreDifference ==  1 && playerA.gameScore >= 3 -> {playerA.advPlayer()
                "Advantage ${playerA.name}"
            }
            else -> "${gameScoreAsString(playerA)}-${gameScoreAsString(playerB)}"
        }
    }

    fun gameScoreAsString( player: Player ): String =
        scores.getOrElse( player.gameScore ) { player.gameScore.toString() }
}