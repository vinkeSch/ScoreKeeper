package com.example.scorekeeper

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.ImageView
import android.widget.TextView


/**
 * Represents a Player in a Tennis game.
 */
class Player( val name: String, var textPoints: TextView, var textSet1: TextView,
              var textSet2: TextView, var textSet3: TextView, var ballIcon: ImageView) {

    var gameScore = 0

    var setScore = 5

    var serving = false

    var tiebreak = false

    val scores = listOf("0", "15", "30", "40")

    var setsWon = 0

    var currentSet = 1

    var currentTextSet = textSet1

    fun increaseGameScore(){
        gameScore++
        if(gameScore <=3 && !tiebreak) textPoints.text = scores[gameScore]
        else if (tiebreak) textPoints.text = gameScore.toString()
    }

    fun wonAGame() {
        setScore++
        currentTextSet.text = setScore.toString()
        gameScore = 0
        textPoints.text = "0"

        serving = !serving
        setIcon()
    }

    fun looseAGame(){
        gameScore = 0
        textPoints.text = "0"

        serving = !serving
        setIcon()
    }

    @SuppressLint("SetTextI18n")
    fun advPlayer(){
        textPoints.text = "AD"
    }

    @SuppressLint("SetTextI18n")
    fun deucePlayer(){
        textPoints.text = "40"
    }

    fun setWon(playerA : Boolean){

        if(playerA) currentTextSet.setTextColor(Color.parseColor("#99B2DD"))
        else currentTextSet.setTextColor(Color.parseColor("#E9AFA3"))
        currentTextSet.typeface = Typeface.DEFAULT_BOLD

        tiebreak = false
        setsWon++
    }

    fun setIcon(){
        if (serving) ballIcon.visibility = View.VISIBLE
        else ballIcon.visibility = View.INVISIBLE
    }

    fun restartStats(){
        setScore = 0
        gameScore = 0
        textPoints.text = "0"
        tiebreak = false
        setsWon = 0

        textSet1.text = "0"
        textSet1.setTextColor(Color.parseColor("#E9ECF5"))
        textSet1.typeface = Typeface.SANS_SERIF

        textSet2.text = "0"
        textSet2.setTextColor(Color.parseColor("#E9ECF5"))
        textSet2.typeface = Typeface.SANS_SERIF
        textSet2.visibility = View.INVISIBLE

        textSet3.text = "0"
        textSet3.setTextColor(Color.parseColor("#E9ECF5"))
        textSet3.typeface = Typeface.SANS_SERIF
        textSet3.visibility = View.INVISIBLE

        currentTextSet = textSet1
        currentSet = 1

        textPoints.visibility = View.VISIBLE
    }

    fun nextSet(){
        currentSet++
        when (currentSet){
            2 -> {
                currentTextSet = textSet2
                textSet2.visibility = View.VISIBLE
            }
            3 -> {
                currentTextSet = textSet3
                textSet3.visibility = View.VISIBLE
            }
        }
    }
}