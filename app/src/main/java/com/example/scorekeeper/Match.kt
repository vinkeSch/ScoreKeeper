package com.example.scorekeeper

import android.content.Context
import android.widget.Toast
import kotlin.math.abs

/**
 * Represents a simplified Tennis Match with only one Set as defined by
 * "A match has one set and a set has many games"
 */
class Match( val playerA: Player, val playerB: Player){

    var scoreMechanism:ScoreMechanism = StandardScoreMechanism(playerA, playerB)

    var matchFinished = false

    fun score(): String = scoreMechanism.computeSetScore()

    var score = ""

    fun pointWonBy(playerName: String) {
        if(!matchFinished){
            val (attacker, defendant) = when ( playerName ) {
                playerA.name -> playerA to playerB
                else -> playerB to playerA
            }

            computeScore(attacker, defendant)

            if ( isMatchTied() ) {
                println("TIEBREAK")
                playerA.tiebreak = true
                playerB.tiebreak = true
                scoreMechanism = TieBreakScoreMechanism(playerA, playerB)
            }
        }

    }

    fun computeScore( attacker: Player, defendant: Player ) {
        attacker.increaseGameScore()

        val setScoreDifference = playerA.gameScore - playerB.gameScore

        when {
            setScoreDifference ==  0 && playerA.gameScore >= 3 && !isMatchTied()-> {playerA.deucePlayer()
                playerB.deucePlayer()
                println("Deuce")
            }
            setScoreDifference == -1 && playerA.gameScore >= 3 && !isMatchTied()-> {playerB.advPlayer()
                println("Advantage ${playerB.name}")
            }
            setScoreDifference ==  1 && playerA.gameScore >= 3 && playerB.gameScore >= 3 && !isMatchTied()-> {playerA.advPlayer()
                println("Advantage ${playerA.name}")
            }
            else -> println("NADA")
        }

        val diff = attacker.gameScore - defendant.gameScore
        if ( diff > 1 && attacker.gameScore >= scoreMechanism.minScoreToWinGame ) {
            if(isMatchTied()){
                println("${attacker.name} WON THE TIEBREAK")
            }
            attacker.wonAGame()
            defendant.looseAGame()

            val matchScoreIsClose = abs(playerA.setScore - playerB.setScore ) < 2

            when {
                (playerA.setScore == 7 || (!matchScoreIsClose && playerA.setScore >= 6))-> {
                    playerA.setWon(true)
                    playerB.tiebreak = false
                    if(playerA.setsWon == 2){
                        //println("${playerA.name} is the Winner")
                        //Toast.makeText(this, "${playerA.name} is the Winner", Toast.LENGTH_SHORT).show()
                        score += "${playerA.setScore}-${playerB.setScore}"
                        matchFinished = true
                    }
                    else {
                        newSet()
                        playerA.nextSet()
                        playerB.nextSet()
                    }

                }
                (playerB.setScore == 7 || (!matchScoreIsClose && playerB.setScore >= 6 )) -> {
                    playerB.setWon(false)
                    playerA.tiebreak = false
                    if(playerB.setsWon == 2){
                        println("${playerB.name} is the Winner")
                        score += "${playerA.setScore}-${playerB.setScore}"
                        matchFinished = true
                    }
                    else {
                        newSet()
                        playerA.nextSet()
                        playerB.nextSet()
                    }
                }
            }
        }
    }

    fun isMatchTied() = playerA.setScore == 6 && playerB.setScore == 6

    fun restartMatch(){
        playerA.restartStats()
        playerB.restartStats()
        matchFinished = false
        score = ""
    }

    fun newSet(){
        score += "${playerA.setScore}-${playerB.setScore} "
        playerA.setScore = 0
        playerB.setScore = 0
        scoreMechanism = StandardScoreMechanism(playerA, playerB)
    }
}