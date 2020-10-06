package com.example.scorekeeper

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_match_landscape.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SpikeFragment] factory method to
 * create an instance of this fragment.
 */
class SpikeFragment : Fragment()  {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var matchScore = ""

    private var gameScoreA = 0
    private var gameScoreB = 0

    private var setsWonA = 0
    private var setsWonB = 0

    private var setOldA = 0
    private var setOldB = 0

    private var currentSet = 1
    private var servingA = false

    private val minScoreToWinSet = 21

    private lateinit var currentTextSetA: TextView
    private lateinit var currentTextSetB: TextView

    private lateinit var playerNameA: TextView
    private lateinit var playerNameB: TextView

    private lateinit var pointsA: TextView
    private lateinit var pointsB: TextView

    private lateinit var set1A: TextView
    private lateinit var set2A: TextView
    private lateinit var set3A: TextView
    private lateinit var set1B: TextView
    private lateinit var set2B: TextView
    private lateinit var set3B: TextView

    private lateinit var ballA: ImageView
    private lateinit var ballB: ImageView
    private lateinit var speech: ToggleButton

    private var currentPoint = ""
    private  var pointNumber = 0
    private  var scoreHistory = mutableListOf<String>()

    private lateinit var tts: TTS

    private var mute = true // No score by voice by default

    private val TAG = "SpikeFragment"

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_spike, container, false)

        // Different trigger events that can be sent when a user interacts with a button.
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener(View.OnKeyListener { _, keyCode, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_UP) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) { // LONG PRESS -> UNDO BUTTON
                    if (keyEvent.eventTime - keyEvent.downTime > ViewConfiguration.getLongPressTimeout()) { // long press
                        Log.d(TAG, "Enter button was long pressed")
                        getPreviousScore()
                        return@OnKeyListener true
                    }
                    else { // POINT WON BY B
                        Log.d(TAG, "Enter button was pressed")
                        pointWonByB()
                        return@OnKeyListener true
                    }
                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) { // LONG PRESS -> RESET BUTTON
                    if (keyEvent.eventTime - keyEvent.downTime > ViewConfiguration.getLongPressTimeout()) { // long press
                        Log.d(TAG, "Volume up button was long pressed")
                        resetMatch()
                        return@OnKeyListener true
                    } else { // POINT WON BY A
                        Log.d(TAG, "Volume up button was pressed")
                        pointWonByA()
                        return@OnKeyListener true
                    }
                }
            }
            else if (keyEvent.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    Log.d(TAG, "Enter button was pressed down")
                    return@OnKeyListener true
                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    Log.d(TAG, "Volume up button was pressed down")
                    return@OnKeyListener true
                }
            }
            false
        })

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        set1A = view.findViewById(R.id.textSet1A)
        set2A = view.findViewById(R.id.textSet2A)
        set3A = view.findViewById(R.id.textSet3A)

        set1B = view.findViewById(R.id.textSet1B)
        set2B = view.findViewById(R.id.textSet2B)
        set3B = view.findViewById(R.id.textSet3B)

        set1A.visibility = View.INVISIBLE
        set1B.visibility = View.INVISIBLE
        set2A.visibility = View.INVISIBLE
        set2B.visibility = View.INVISIBLE
        set3A.visibility = View.INVISIBLE
        set3B.visibility = View.INVISIBLE

        ballA = view.findViewById(R.id.ballA)
        ballB = view.findViewById(R.id.ballB)
        ballB.visibility= View.INVISIBLE

        speech = view.findViewById(R.id.imageVolume)
        tts = TTS(requireActivity(), true) // Spanish TTS

        currentTextSetA = set1A
        currentTextSetB = set1B

        pointsA = view.findViewById(R.id.textPointsA)
        pointsB = view.findViewById(R.id.textPointsB)
        pointsA.text = "0"
        pointsB.text = "0"

        // Receive the data from caller fragment/activity
        playerNameA = view.findViewById(R.id.textPlayerA)
        playerNameA.text = arguments?.getString("nameA")

        playerNameB = view.findViewById(R.id.textPlayerB)
        playerNameB.text = arguments?.getString("nameB")

        servingA = arguments?.getBoolean("serve")!!
        setIcon()

        // Reset match
        val iconRestart = view.findViewById<ImageView>(R.id.iconRestart)
        iconRestart.setOnClickListener {
            Toast.makeText(requireActivity(), "Long press to restart match", Toast.LENGTH_SHORT).show()
        }

        iconRestart.setOnLongClickListener{
            resetMatch()
            true
        }

        // Undo last point
        val iconUndo = view.findViewById<ImageView>(R.id.iconUndo)
        iconUndo.setOnClickListener {
            getPreviousScore()
        }

        pointsA.setOnClickListener {
            pointWonByA()
        }

        pointsB.setOnClickListener {
            pointWonByB()
        }

        speech.setOnClickListener {
            mute = !mute
        }

        // Initial point
        addPointToHistory()

        return view
}

    private fun getPreviousScore() {
        if (pointNumber != 1) {
            val scoreValues = (scoreHistory[pointNumber - 2].split(" "))
            when (scoreValues[0]) { // number of set playing
                "1" -> { // 1st set
                    if (setsWonA == 1) { // If Player A had won the first set
                        currentSet--
                        setsWonA = 0
                        set1A.setTextColor(Color.parseColor("#E9ECF5"))
                        set1A.typeface = Typeface.SANS_SERIF
                        currentTextSetA = set1A
                        currentTextSetB = set1B
                        matchScore = ""
                        set1A.visibility = View.INVISIBLE
                        set1B.visibility = View.INVISIBLE
                    } else if (setsWonB == 1) {
                        currentSet--
                        setsWonB = 0
                        set1B.setTextColor(Color.parseColor("#E9ECF5"))
                        set1B.typeface = Typeface.SANS_SERIF
                        currentTextSetA = set1A
                        currentTextSetB = set1B
                        matchScore = ""
                        set1A.visibility = View.INVISIBLE
                        set1B.visibility = View.INVISIBLE
                    }
                    gameScoreA = scoreValues[7].toInt()
                    gameScoreB = scoreValues[8].toInt()
                }
                "2" -> { // 2nd set
                    val previousSet2A = set2A.text.toString().toInt()
                    val previousSet2B = set2B.text.toString().toInt()
                    val matchScoreIsClose = kotlin.math.abs(previousSet2A - previousSet2B) < 2
                    if ((!matchScoreIsClose && previousSet2A >= 21)) {
                        currentSet--
                        setsWonA--
                        set2A.setTextColor(Color.parseColor("#E9ECF5"))
                        set2A.typeface = Typeface.SANS_SERIF
                        currentTextSetA = set2A
                        currentTextSetB = set2B
                        matchScore = matchScore.substring(0, 3) // TODO
                        set2A.visibility = View.INVISIBLE
                        set2B.visibility = View.INVISIBLE
                    } else if ((!matchScoreIsClose && previousSet2B >= 21)) {
                        currentSet--
                        setsWonB--
                        set2B.setTextColor(Color.parseColor("#E9ECF5"))
                        set2B.typeface = Typeface.SANS_SERIF
                        currentTextSetA = set2A
                        currentTextSetB = set2B
                        matchScore = matchScore.substring(0, 3)
                        set2A.visibility = View.INVISIBLE
                        set2B.visibility = View.INVISIBLE
                    }
                    gameScoreA = scoreValues[7].toInt()
                    gameScoreB = scoreValues[8].toInt()
                }
                else -> { // 3rd set
                    currentSet = 3
                    gameScoreA = scoreValues[7].toInt()
                    gameScoreB = scoreValues[8].toInt()
                }
            }

            // Restore fragment values
            set1A.text = scoreValues[1]
            set1B.text = scoreValues[2]
            set2A.text = scoreValues[3]
            set2B.text = scoreValues[4]
            set3A.text = scoreValues[5]
            set3B.text = scoreValues[6]
            pointsA.text = scoreValues[7]
            pointsB.text = scoreValues[8]
            pointNumber--

            servingA = scoreValues[9] == "true"
            setIcon()
        } else Toast.makeText(
            requireActivity(),
            "First point of the match!\nLet's play!",
            Toast.LENGTH_SHORT
        ).show()
    }

    @SuppressLint("SetTextI18n")
    fun pointWonByA() {
        gameScoreA++
        servingA = true
        setIcon()

        val diff = gameScoreA - gameScoreB
        if ( diff > 1 && gameScoreA >= minScoreToWinSet ) { // SET WON
            setWonByA()
            // check if the match has been won
            if(setsWonA == 2){ // MATCH WON
                matchWonByA()
                if (!mute) matchByVoice()
            }
            else { // NEXT SET
                // set to voice
                if (!mute) tts.speakOut("Set ${playerNameA.text}. $gameScoreA $gameScoreB")
                nextSet()
            }
        }
        else{ // POINT WON; NOT THE SET
            pointsA.text = gameScoreA.toString()
            if (!mute){
                when {
                    (gameScoreA > gameScoreB) -> tts.speakOut("$gameScoreA $gameScoreB, ${playerNameA.text}")
                    (gameScoreA < gameScoreB) -> tts.speakOut("$gameScoreB $gameScoreA, ${playerNameB.text}")
                    else -> tts.speakOut("$gameScoreA, iguales")
                }
            }
        }
        addPointToHistory() //update the score history
    }

    private fun matchByVoice() {
        if(setsWonA == 2){ // Player A won
            when (currentSet){
                2 -> tts.speakOut("Set y partido ${playerNameA.text}. ${set1A.text} ${set1B.text}, ${set2A.text} ${set2B.text}")
                3 -> tts.speakOut("Set y partido ${playerNameA.text}. ${set1A.text} ${set1B.text}, ${set2A.text} ${set2B.text}, ${set3A.text} ${set3B.text}")
            }
        }
        else { // Player B won
            when (currentSet){
                2 -> tts.speakOut("Set y partido ${playerNameB.text}. ${set1B.text} ${set1A.text}, ${set2B.text} ${set2A.text}")
                3 -> tts.speakOut("Set y partido ${playerNameB.text}. ${set1B.text} ${set1A.text}, ${set2B.text} ${set2A.text}, ${set3B.text} ${set3A.text}")
            }
        }
    }

    private fun setWonByA() {
        currentTextSetA.setTextColor(Color.parseColor("#99B2DD"))
        currentTextSetA.typeface = Typeface.DEFAULT_BOLD
        currentTextSetA.text = gameScoreA.toString()
        currentTextSetB.text = gameScoreB.toString()
        setsWonA++
        matchScore += "${gameScoreA}-${gameScoreB} "
    }

    private fun matchWonByA() {
        pointsA.visibility = View.INVISIBLE
        pointsB.visibility = View.INVISIBLE
        ballA.visibility = View.INVISIBLE
        ballB.visibility = View.INVISIBLE
        iconUndo.visibility = View.INVISIBLE

        when (currentSet){
            2 -> {
                set2A.visibility = View.VISIBLE
                set2B.visibility = View.VISIBLE
            }
            3 -> {
                set3A.visibility = View.VISIBLE
                set3B.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun pointWonByB() {
        gameScoreB++
        servingA = false
        setIcon()

        val diff = gameScoreB - gameScoreA
        if ( diff > 1 && gameScoreB >= minScoreToWinSet ) { // SET WON
            setWonByB()
            // check if the match has been won
            if(setsWonB == 2){ // MATCH WON
                matchWonByB()
                if (!mute) matchByVoice()
            }
            else { // NEXT SET
                // set to voice
                if (!mute) tts.speakOut("Set ${playerNameB.text}. $gameScoreB $gameScoreA")
                nextSet()
            }
        }
        else{ // POINT WON; NOT THE SET
            pointsB.text = gameScoreB.toString()
            if (!mute) {
                when {
                    (gameScoreA > gameScoreB) -> tts.speakOut("$gameScoreA $gameScoreB, ${playerNameA.text}")
                    (gameScoreA < gameScoreB) -> tts.speakOut("$gameScoreB $gameScoreA, ${playerNameB.text}")
                    else -> tts.speakOut("$gameScoreA, iguales")
                }
            }
        }
        addPointToHistory() //update the score history
    }

    private fun setWonByB() {
        currentTextSetB.setTextColor(Color.parseColor("#E9AFA3"))
        currentTextSetB.typeface = Typeface.DEFAULT_BOLD
        currentTextSetA.text = gameScoreA.toString()
        currentTextSetB.text = gameScoreB.toString()
        setsWonB++
        matchScore += "${gameScoreA}-${gameScoreB} "
    }

    private fun matchWonByB() {
        pointsA.visibility = View.INVISIBLE
        pointsB.visibility = View.INVISIBLE
        ballA.visibility = View.INVISIBLE
        ballB.visibility = View.INVISIBLE
        iconUndo.visibility = View.INVISIBLE

        when (currentSet){
            2 -> {
                set2A.visibility = View.VISIBLE
                set2B.visibility = View.VISIBLE
            }
            3 -> {
                set3A.visibility = View.VISIBLE
                set3B.visibility = View.VISIBLE
            }
        }
    }

    private fun nextSet() {
        currentSet++
        when (currentSet){
            2 -> {
                currentTextSetA = set2A
                set1A.visibility = View.VISIBLE
                currentTextSetB = set2B
                set1B.visibility = View.VISIBLE
            }
            3 -> {
                currentTextSetA = set3A
                set2A.visibility = View.VISIBLE
                currentTextSetB = set3B
                set2B.visibility = View.VISIBLE
            }
        }
        // reset gameScores
        gameScoreA = 0
        pointsA.text = "0"
        gameScoreB = 0
        pointsB.text = "0"
    }

    private fun setIcon(){
        if (servingA) {
            ballA.visibility = View.VISIBLE
            ballB.visibility = View.INVISIBLE
        }
        else {
            ballB.visibility = View.VISIBLE
            ballA.visibility = View.INVISIBLE
        }
    }

    private fun resetMatch() {
        gameScoreA = 0
        gameScoreB = 0

        pointsA.text = "0"
        pointsB.text = "0"

        setsWonA = 0
        setsWonB = 0

        set1A.text = "0"
        set1A.setTextColor(Color.parseColor("#E9ECF5"))
        set1A.typeface = Typeface.SANS_SERIF

        set2A.text = "0"
        set2A.setTextColor(Color.parseColor("#E9ECF5"))
        set2A.typeface = Typeface.SANS_SERIF
        set2A.visibility = View.INVISIBLE

        set3A.text = "0"
        set3A.setTextColor(Color.parseColor("#E9ECF5"))
        set3A.typeface = Typeface.SANS_SERIF
        set3A.visibility = View.INVISIBLE

        set1B.text = "0"
        set1B.setTextColor(Color.parseColor("#E9ECF5"))
        set1B.typeface = Typeface.SANS_SERIF

        set2B.text = "0"
        set2B.setTextColor(Color.parseColor("#E9ECF5"))
        set2B.typeface = Typeface.SANS_SERIF
        set2B.visibility = View.INVISIBLE

        set3B.text = "0"
        set3B.setTextColor(Color.parseColor("#E9ECF5"))
        set3B.typeface = Typeface.SANS_SERIF
        set3B.visibility = View.INVISIBLE

        currentSet = 1
        currentTextSetA = set1A
        currentTextSetB = set1B

        pointsA.visibility = View.VISIBLE
        pointsB.visibility = View.VISIBLE
        ballA.visibility = View.VISIBLE
        ballB.visibility = View.INVISIBLE
        servingA = true

        matchScore = ""

        pointNumber = 0
        addPointToHistory() // initial point
        iconUndo.visibility = View.VISIBLE

        Toast.makeText(requireActivity(), "Match restarted", Toast.LENGTH_SHORT).show()
    }

    private fun addPointToHistory(){
        currentPoint = "$currentSet ${set1A.text} ${set1B.text} ${set2A.text} ${set2B.text} ${set3A.text} ${set3B.text} ${pointsA.text} ${pointsB.text} $servingA"
        scoreHistory.add(pointNumber, currentPoint)
        pointNumber++
    }

    override fun onDestroy() {
        // Shutdown TTS
        tts.tts.stop()
        tts.tts.shutdown()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onDestroy()
        }
    }