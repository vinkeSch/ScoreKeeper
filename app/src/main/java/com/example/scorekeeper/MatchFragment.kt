package com.example.scorekeeper

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.scorekeeper.databinding.FragmentMatchBinding

import kotlin.math.abs

class MatchFragment : Fragment() {

    private val scores = listOf("0", "15", "30", "40")
    private val scoreMap = mapOf("0" to 0, "15" to 1, "30" to 2, "40" to 3)
    private var scoreVoiceMap = mapOf<String, String>()

    private var matchScore = ""

    private var gameScoreA = 0
    private var gameScoreB = 0
    private var setScoreA = 0
    private var setScoreB = 0
    private var setsWonA = 0
    private var setsWonB = 0

    private var setOldA = 0
    private var setOldB = 0

    private var isTiebreak = false
    private var currentSet = 1
    private var servingA = false

    private var minScoreToWinGame = 4
    private var setsToWinMatch = 2 // By default 3-set match is played

    private lateinit var currentTextSetA: TextView
    private lateinit var currentTextSetB: TextView

    private lateinit var playerNameA: TextView
    private lateinit var playerNameB: TextView

    private lateinit var pointsA: TextView
    private lateinit var pointsB: TextView

    private lateinit var set1A: TextView
    private lateinit var set2A: TextView
    private lateinit var set3A: TextView
    private lateinit var set4A: TextView
    private lateinit var set5A: TextView

    private lateinit var set1B: TextView
    private lateinit var set2B: TextView
    private lateinit var set3B: TextView
    private lateinit var set4B: TextView
    private lateinit var set5B: TextView

    private lateinit var ballA: ImageView
    private lateinit var ballB: ImageView

    private var currentPoint = ""
    private var pointNumber = 0
    private var scoreHistory = mutableListOf<String>()

    private lateinit var tts: TTS

    private var tiebreakPointNumber = 0
    private var mute = true // No score by voice by default

    private val TAG = "MatchFragment"

    private var _binding: FragmentMatchBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //val view = inflater.inflate(R.layout.fragment_match, container, false)

        _binding = FragmentMatchBinding.inflate(inflater, container, false)
        val view = binding.root

        // Different trigger events that can be sent when a user interacts with a button.
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener(View.OnKeyListener { _, keyCode, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_UP) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) { // LONG PRESS -> RESET BUTTON
                    if (keyEvent.eventTime - keyEvent.downTime > ViewConfiguration.getLongPressTimeout()) { // long press
                        Log.d(TAG, "Android button was long pressed")
                        resetMatch()
                        return@OnKeyListener true
                    } else { // POINT WON BY B
                        Log.d(TAG, "Android button was pressed")
                        pointWonByB()
                        return@OnKeyListener true
                    }
                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) { // LONG PRESS -> UNDO BUTTON
                    if (keyEvent.eventTime - keyEvent.downTime > ViewConfiguration.getLongPressTimeout()) { // long press
                        Log.d(TAG, "iOS button was long pressed")
                        if (pointNumber > 1) getPreviousScore()
                        else Toast.makeText(
                            requireActivity(),
                            "First point of the match!\nLet's play!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@OnKeyListener true
                    } else { // POINT WON BY A
                        Log.d(TAG, "iOS button was pressed")
                        pointWonByA()
                        return@OnKeyListener true
                    }
                }
            }
            else if (keyEvent.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    Log.d(TAG, "Android button was pressed down")
                    return@OnKeyListener true
                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    Log.d(TAG, "iOS button was pressed down")
                    return@OnKeyListener true
                }
            }
            false
        })

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // Initialize the UI elements
        set1A = binding.textSet1A
        set2A = binding.textSet2A
        set3A = binding.textSet3A
        set4A = binding.textSet4A
        set5A = binding.textSet5A

        set1B = binding.textSet1B
        set2B = binding.textSet2B
        set3B = binding.textSet3B
        set4B = binding.textSet4B
        set5B = binding.textSet5B

        set2A.visibility = View.INVISIBLE
        set3A.visibility = View.INVISIBLE
        set4A.visibility = View.INVISIBLE
        set5A.visibility = View.INVISIBLE

        set2B.visibility = View.INVISIBLE
        set3B.visibility = View.INVISIBLE
        set4B.visibility = View.INVISIBLE
        set5B.visibility = View.INVISIBLE

        ballA = binding.ballA
        ballB = binding.ballB
        ballB.visibility= View.INVISIBLE

        tts = TTS(requireActivity(), false) // English TTS

        currentTextSetA = set1A
        currentTextSetB = set1B

        pointsA = binding.textPointsA
        pointsB = binding.textPointsB
        pointsA.text = "0"
        pointsB.text = "0"

        // Receive the data from caller fragment/activity
        playerNameA = binding.textPlayerA
        playerNameA.text = arguments?.getString("nameA")

        playerNameB = binding.textPlayerB
        playerNameB.text = arguments?.getString("nameB")

        servingA = arguments?.getBoolean("serve")!!
        setIcon()

        when(arguments?.getInt("sets")!!) {
            1 -> setsToWinMatch = 1
            3 -> setsToWinMatch = 2
            5 -> setsToWinMatch = 3
        }

        scoreVoiceMap = mapOf(
            "0-15" to "Love fifteen", "0-30" to "Love thirty",
            "0-40" to "Love forty", "15-0" to "Fifteen love", "30-0" to "Thirty love",
            "40-0" to "Forty love", "15-40" to "Fifteen forty", "15-15" to "Fifteen all",
            "30-15" to "Thirty fifteen", "15-30" to "Fifteen thirty", "30-30" to "Thirty all",
            "40-30" to "Forty thirty", "30-40" to "Thirty forty",
            "40-15" to "Forty fifteen"
        )

        // Reset match
        binding.iconRestart.setOnClickListener {
            Toast.makeText(requireActivity(),
                "Long press to restart match", Toast.LENGTH_SHORT).show()
        }

        binding.iconRestart.setOnLongClickListener{
            resetMatch()
            true
        }

        // Undo last point
        binding.iconUndo.setOnClickListener {
            if (pointNumber > 1) getPreviousScore()
            else Toast.makeText(
                requireActivity(),
                "First point of the match!\nLet's play!",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.textPointsA.setOnClickListener { pointWonByA() }

        binding.textPointsB.setOnClickListener { pointWonByB() }

        binding.buttonSpeech.setOnClickListener { mute = !mute }

        // Initial point
        addPointToHistory()

        return view
    }

    private fun getPreviousScore(){

        val scoreValues = (scoreHistory[pointNumber - 2].split(" "))
        when(scoreValues[0]){ // number of set playing
            "1" -> { // 1st set
                if (setsWonA == 1) {
                    currentSet--
                    setsWonA = 0
                    set1A.setTextColor(Color.parseColor("#E9ECF5"))
                    set1A.typeface = Typeface.SANS_SERIF
                    currentTextSetA = set1A
                    currentTextSetB = set1B
                    matchScore = ""
                } else if (setsWonB == 1) {
                    currentSet--
                    setsWonB = 0
                    set1B.setTextColor(Color.parseColor("#E9ECF5"))
                    set1B.typeface = Typeface.SANS_SERIF
                    currentTextSetA = set1A
                    currentTextSetB = set1B
                    matchScore = ""
                }
                set2A.visibility = View.INVISIBLE
                set2B.visibility = View.INVISIBLE
                setScoreA = scoreValues[1].toInt()
                setScoreB = scoreValues[2].toInt()
            }
            "2" -> { // 2nd set
                val previousSet2A = set2A.text.toString().toInt()
                val previousSet2B = set2B.text.toString().toInt()
                val matchScoreIsClose = abs(previousSet2A - previousSet2B) < 2
                if (previousSet2A == 7 || (!matchScoreIsClose && previousSet2A >= 6)) {
                    currentSet--
                    setsWonA--
                    set2A.setTextColor(Color.parseColor("#E9ECF5"))
                    set2A.typeface = Typeface.SANS_SERIF
                    currentTextSetA = set2A
                    currentTextSetB = set2B
                    matchScore = matchScore.substring(0, 3)
                } else if (previousSet2B == 7 || (!matchScoreIsClose && previousSet2B >= 6)) {
                    currentSet--
                    setsWonB--
                    set2B.setTextColor(Color.parseColor("#E9ECF5"))
                    set2B.typeface = Typeface.SANS_SERIF
                    currentTextSetA = set2A
                    currentTextSetB = set2B
                    matchScore = matchScore.substring(0, 3)
                }
                set3A.visibility = View.INVISIBLE
                set3B.visibility = View.INVISIBLE
                setScoreA = scoreValues[3].toInt()
                setScoreB = scoreValues[4].toInt()
            }
            "3" -> { // 3rd set
                val previousSet3A = set3A.text.toString().toInt()
                val previousSet3B = set3B.text.toString().toInt()
                val matchScoreIsClose = abs(previousSet3A - previousSet3B) < 2
                if (previousSet3A == 7 || (!matchScoreIsClose && previousSet3A >= 6)) {
                    currentSet--
                    setsWonA--
                    set3A.setTextColor(Color.parseColor("#E9ECF5"))
                    set3A.typeface = Typeface.SANS_SERIF
                } else if (previousSet3B == 7 || (!matchScoreIsClose && previousSet3B >= 6)) {
                    currentSet--
                    setsWonB--
                    set3B.setTextColor(Color.parseColor("#E9ECF5"))
                    set3B.typeface = Typeface.SANS_SERIF
                }
                currentTextSetA = set3A
                currentTextSetB = set3B
                matchScore = matchScore.substring(0, 7)

                set4A.visibility = View.INVISIBLE
                set4B.visibility = View.INVISIBLE
                setScoreA = scoreValues[5].toInt()
                setScoreB = scoreValues[6].toInt()
            }
            "4" -> { // 4th set
                val previousSet4A = set4A.text.toString().toInt()
                val previousSet4B = set4B.text.toString().toInt()
                val matchScoreIsClose = abs(previousSet4A - previousSet4B) < 2
                if (previousSet4A == 7 || (!matchScoreIsClose && previousSet4A >= 6)) {
                    currentSet--
                    setsWonA--
                    set4A.setTextColor(Color.parseColor("#E9ECF5"))
                    set4A.typeface = Typeface.SANS_SERIF
                } else if (previousSet4B == 7 || (!matchScoreIsClose && previousSet4B >= 6)) {
                    currentSet--
                    setsWonB--
                    set4B.setTextColor(Color.parseColor("#E9ECF5"))
                    set4B.typeface = Typeface.SANS_SERIF
                }
                currentTextSetA = set4A
                currentTextSetB = set4B
                matchScore = matchScore.substring(0, 11)

                set5A.visibility = View.INVISIBLE
                set5B.visibility = View.INVISIBLE
                setScoreA = scoreValues[7].toInt()
                setScoreB = scoreValues[8].toInt()
            }
            else -> { // 5rd set
                currentSet = 5
                setScoreA = scoreValues[9].toInt()
                setScoreB = scoreValues[10].toInt()
            }
        }

        if(!(setScoreA == 6 && setScoreB == 6 )){ // no tiebreak
            if ((scoreValues[11] != "AD") && (scoreValues[12] != "AD") ) {
                gameScoreA = scoreMap[scoreValues[11]] ?: error("")
                gameScoreB = scoreMap[scoreValues[12]] ?: error("")
            }
            else if (scoreValues[11] == "AD") {
                gameScoreA = 5
                gameScoreB = 4
            }
            else if (scoreValues[12] == "AD") {
                gameScoreA = 4
                gameScoreB = 5
            }
        }
        else { // tiebreak
            tiebreakPointNumber--
            gameScoreA = scoreValues[11].toInt()
            gameScoreB = scoreValues[12].toInt()
        }
        // Restore fragment values
        set1A.text = scoreValues[1]
        set1B.text = scoreValues[2]
        set2A.text = scoreValues[3]
        set2B.text = scoreValues[4]
        set3A.text = scoreValues[5]
        set3B.text = scoreValues[6]
        set4A.text = scoreValues[7]
        set4B.text = scoreValues[8]
        set5A.text = scoreValues[9]
        set5B.text = scoreValues[10]
        pointsA.text = scoreValues[11]
        pointsB.text = scoreValues[12]
        pointNumber--

        servingA = scoreValues[13] == "true"
        setIcon()

        if ( setScoreA == 6 && setScoreB == 6 ) {
            isTiebreak = true
            minScoreToWinGame = 7
        }
        else {
            isTiebreak = false
            minScoreToWinGame = 4
        }
    }

    @SuppressLint("SetTextI18n")
    fun pointWonByA() {
        gameScoreA++

        if (!isTiebreak){ // NOT TIEBREAK
            if(gameScoreA <=3 && !(gameScoreA == 3 && gameScoreB == 3)) pointsA.text = scores[gameScoreA]
            else { // gamescore > 3 -> deuce/adv scoring
                when (gameScoreA - gameScoreB) {
                    0 -> {
                        pointsA.text = "40"
                        pointsB.text = "40"
                        if (!mute) tts.speakOut("Deuce")
                    }
                    -1 -> {
                        pointsB.text = "AD"
                        if (!mute) tts.speakOut("Advantage ${playerNameB.text}")
                    }
                    1 -> {
                        pointsA.text = "AD"
                        if (!mute) tts.speakOut("Advantage ${playerNameA.text}")
                    }
                }
            }
        }
        else { // TIEBREAK
            if(tiebreakPointNumber % 2 == 1){ // change server after 2 points
                servingA = !servingA
                setIcon()
            }
            tiebreakPointNumber++
            pointsA.text = gameScoreA.toString()
            if (!mute) {
                when {
                    (gameScoreA > gameScoreB) -> tts.speakOut("$gameScoreA $gameScoreB." +
                            " ${playerNameA.text}")
                    (gameScoreA < gameScoreB) -> tts.speakOut("$gameScoreB $gameScoreA. " +
                            " ${playerNameB.text}")
                    else -> tts.speakOut("$gameScoreB all")
                }
            }
        }

        val diff = gameScoreA - gameScoreB
        if ( diff > 1 && gameScoreA >= minScoreToWinGame ) {
            gameWonByA()
            // check if the set has been won
            val matchScoreIsClose = abs(setScoreA - setScoreB) < 2

            if (setScoreA == 7 || (!matchScoreIsClose && setScoreA >= 6)) { // won the set
                    setWonByA()
                    if(setsWonA == setsToWinMatch){ // MATCH WON
                        matchWonByA()
                        if (!mute) matchScoreToSpeech()
                    }
                    else { // NEXT SET
                        // set to voice
                        if (!mute) tts.speakOut("Game and Set ${playerNameA.text}. $setOldA $setOldB")
                        nextSet()
                    }
            }
            else { // GAME WON; NOT THE SET
                if ( setScoreA == 6 && setScoreB == 6 ) {
                    isTiebreak = true
                    tiebreakPointNumber = 1
                    minScoreToWinGame = 7
                }
                if (!mute){
                    tts.speakOut("Game ${playerNameA.text},")
                    tts.tts.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, null)
                    setScoreToSpeech()
                }

            }
        }
        else{ // POINT WON; NOT THE GAME
            if (!mute){
                if (servingA) scoreVoiceMap["${pointsA.text}-${pointsB.text}"]?.let { tts.speakOut(it) }
                else scoreVoiceMap["${pointsB.text}-${pointsA.text}"]?.let { tts.speakOut(it) }
            }
        }
        addPointToHistory() //update the score history
    }

    private fun setScoreToSpeech() { // Set score in games to speech
        if ( setScoreA == 6 && setScoreB == 6 ) {
            tts.speakOutAdd("Six games all. Tie break")
        }
        else{
            when{
                (setScoreA > setScoreB) -> {
                    if (setScoreA > 1) tts.speakOutAdd("${playerNameA.text} leads $setScoreA games to $setScoreB")
                    else tts.speakOutAdd("${playerNameA.text} leads $setScoreA game to $setScoreB")
                }
                (setScoreA < setScoreB) -> {
                    if (setScoreB > 1) tts.speakOutAdd("${playerNameB.text} leads $setScoreB games to $setScoreA")
                    else tts.speakOutAdd("${playerNameB.text} leads $setScoreB game to $setScoreA")
                }
                else -> {
                    if (setScoreA > 1) tts.speakOutAdd("$setScoreA games all")
                    else tts.speakOutAdd("$setScoreA game all")
                }
            }
        }
    }

    private fun matchScoreToSpeech() {
        if(setsWonA == setsToWinMatch){ // Player A won
            when (currentSet){
                1 -> tts.speakOut("Game, set and match ${playerNameA.text}. ${set1A.text} " +
                        "${set1B.text}")
                2 -> tts.speakOut("Game, set and match ${playerNameA.text}. ${set1A.text} " +
                        "${set1B.text}, ${set2A.text} ${set2B.text}")
                3 -> tts.speakOut("Game, set and match ${playerNameA.text}. ${set1A.text} " +
                        "${set1B.text}, ${set2A.text} ${set2B.text}, ${set3A.text} ${set3B.text}")
                4 -> tts.speakOut("Game, set and match ${playerNameA.text}. ${set1A.text} " +
                        "${set1B.text}, ${set2A.text} ${set2B.text}, ${set3A.text} " +
                        "${set3B.text}, ${set4A.text} ${set4B.text}")
                5 -> tts.speakOut("Game, set and match ${playerNameA.text}. ${set1A.text} " +
                        "${set1B.text}, ${set2A.text} ${set2B.text}, ${set3A.text} " +
                        "${set3B.text}, ${set4A.text} ${set4B.text}, ${set5A.text} ${set5B.text}")
            }
        }
        else { // Player B won
            when (currentSet){
                1 -> tts.speakOut("Game, set and match ${playerNameB.text}. ${set1B.text} " +
                        "${set1A.text}")
                2 -> tts.speakOut("Game, set and match ${playerNameB.text}. ${set1B.text} " +
                        "${set1A.text}, ${set2B.text} ${set2A.text}")
                3 -> tts.speakOut("Game, set and match ${playerNameB.text}. ${set1B.text} " +
                        "${set1A.text}, ${set2B.text} ${set2A.text}, ${set3B.text} ${set3A.text}")
                4 -> tts.speakOut("Game, set and match ${playerNameB.text}. ${set1B.text} " +
                        "${set1A.text}, ${set2B.text} ${set2A.text}, ${set3B.text} " +
                        "${set3A.text}, ${set4B.text} ${set4A.text}")
                5 -> tts.speakOut("Game, set and match ${playerNameB.text}. ${set1B.text} " +
                        "${set1A.text}, ${set2B.text} ${set2A.text}, ${set3B.text} " +
                        "${set3A.text}, ${set4B.text} ${set4A.text}, ${set5B.text} ${set5A.text}")
            }
        }
    }

    private fun gameWonByA() {
        setScoreA++
        currentTextSetA.text = setScoreA.toString()
        gameScoreA = 0
        pointsA.text = "0"
        gameScoreB = 0
        pointsB.text = "0"
        // change server
        servingA = !servingA
        setIcon()
    }

    private fun setWonByA() {
        currentTextSetA.setTextColor(Color.parseColor("#99B2DD"))
        currentTextSetA.typeface = Typeface.DEFAULT_BOLD
        setsWonA++
        matchScore += "${setScoreA}-${setScoreB} "
        isTiebreak = false
        setOldB = setScoreB
        setOldA = setScoreA
        setScoreA = 0
        setScoreB = 0
        minScoreToWinGame = 4
    }

    private fun matchWonByA() {
        Toast.makeText(
            requireActivity(),
            "${playerNameA.text} is the Winner \n $matchScore ",
            Toast.LENGTH_LONG
        ).show()
        pointsA.visibility = View.INVISIBLE
        pointsB.visibility = View.INVISIBLE
        ballA.visibility = View.INVISIBLE
        ballB.visibility = View.INVISIBLE
        binding.iconUndo.visibility = View.INVISIBLE
    }

    @SuppressLint("SetTextI18n")
    fun pointWonByB() {
        gameScoreB++

        if (!isTiebreak){ // NOT TIEBREAK
            if(gameScoreB <=3 && !(gameScoreA == 3 && gameScoreB == 3)) pointsB.text = scores[gameScoreB]
            else { // gamescore > 3 -> deuce/adv scoring
                when (gameScoreA - gameScoreB) {
                    0 -> {
                        pointsA.text = "40"
                        pointsB.text = "40"
                        if (!mute) tts.speakOut("Deuce")
                    }
                    -1 -> {
                        pointsB.text = "AD"
                        if (!mute) tts.speakOut("Advantage ${playerNameB.text}")
                    }
                    1 -> {
                        pointsA.text = "AD"
                        if (!mute) tts.speakOut("Advantage ${playerNameA.text}")
                    }
                }
            }
        }
        else { // TIEBREAK
            pointsB.text = gameScoreB.toString()
            if(tiebreakPointNumber % 2 == 1){ // change server after 2 points
                servingA = !servingA
                setIcon()
            }
            tiebreakPointNumber++
            if (!mute){
                when {
                    (gameScoreA > gameScoreB) -> tts.speakOut("$gameScoreA $gameScoreB. ${playerNameA.text}")
                    (gameScoreA < gameScoreB) -> tts.speakOut("$gameScoreB $gameScoreA. ${playerNameB.text}")
                    else -> tts.speakOut("$gameScoreB all")
                }
            }
        }

        val diff = gameScoreB - gameScoreA
        if ( diff > 1 && gameScoreB >= minScoreToWinGame ) {
            gameWonByB()
            // check if the set has been won
            val matchScoreIsClose = abs(setScoreA - setScoreB) < 2
            if(setScoreB == 7 || (!matchScoreIsClose && setScoreB >= 6)) {
                // won the set
                    setWonByB()
                    if(setsWonB == setsToWinMatch){ // MATCH WON
                        matchWonByB()
                        if(!mute) matchScoreToSpeech()
                    }
                    else { // NEXT SET
                        // set to voice
                        if(!mute) tts.speakOut("Game and Set ${playerNameB.text}. $setOldB $setOldA")
                        nextSet()
                    }

            }
            else { // GAME WON; NOT THE SET
                if ( setScoreA == 6 && setScoreB == 6 ) {
                    isTiebreak = true
                    tiebreakPointNumber = 1
                    minScoreToWinGame = 7
                }
                if(!mute){
                    tts.speakOut("Game ${playerNameB.text}.")
                    tts.tts.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, null)
                    setScoreToSpeech()
                }
            }
        }
        else{ // POINT WON; NOT THE SET
            if(!mute){
                if (servingA) scoreVoiceMap["${pointsA.text}-${pointsB.text}"]?.let { tts.speakOut(it) }
                else scoreVoiceMap["${pointsB.text}-${pointsA.text}"]?.let { tts.speakOut(it) }
            }
        }
        addPointToHistory() //update the score history
    }

    private fun gameWonByB() {
        setScoreB++
        currentTextSetB.text = setScoreB.toString()
        gameScoreB = 0
        pointsB.text = "0"
        gameScoreA = 0
        pointsA.text = "0"
        // change server
        servingA = !servingA
        setIcon()
    }

    private fun setWonByB() {
        currentTextSetB.setTextColor(Color.parseColor("#E9AFA3"))
        currentTextSetB.typeface = Typeface.DEFAULT_BOLD
        setsWonB++
        matchScore += "${setScoreA}-${setScoreB} "
        isTiebreak = false
        setOldB = setScoreB
        setOldA = setScoreA
        setScoreA = 0
        setScoreB = 0
        minScoreToWinGame = 4
    }

    private fun matchWonByB() {
        Toast.makeText(
            requireActivity(),
            "${playerNameB.text} is the Winner \n $matchScore ",
            Toast.LENGTH_LONG
        ).show()
        pointsA.visibility = View.INVISIBLE
        pointsB.visibility = View.INVISIBLE
        ballA.visibility = View.INVISIBLE
        ballB.visibility = View.INVISIBLE
        binding.iconUndo.visibility = View.INVISIBLE
    }

    private fun nextSet() {
        currentSet++
        when (currentSet){
            2 -> {
                currentTextSetA = set2A
                set2A.visibility = View.VISIBLE
                currentTextSetB = set2B
                set2B.visibility = View.VISIBLE
            }
            3 -> {
                currentTextSetA = set3A
                set3A.visibility = View.VISIBLE
                currentTextSetB = set3B
                set3B.visibility = View.VISIBLE
            }
            4 -> {
                currentTextSetA = set4A
                set4A.visibility = View.VISIBLE
                currentTextSetB = set4B
                set4B.visibility = View.VISIBLE
            }
            5 -> {
                currentTextSetA = set5A
                set5A.visibility = View.VISIBLE
                currentTextSetB = set5B
                set5B.visibility = View.VISIBLE
            }
        }
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
        setScoreA = 0
        setScoreB = 0

        pointsA.text = "0"
        pointsB.text = "0"

        isTiebreak = false
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

        set4A.text = "0"
        set4A.setTextColor(Color.parseColor("#E9ECF5"))
        set4A.typeface = Typeface.SANS_SERIF
        set4A.visibility = View.INVISIBLE

        set5A.text = "0"
        set5A.setTextColor(Color.parseColor("#E9ECF5"))
        set5A.typeface = Typeface.SANS_SERIF
        set5A.visibility = View.INVISIBLE

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

        set4B.text = "0"
        set4B.setTextColor(Color.parseColor("#E9ECF5"))
        set4B.typeface = Typeface.SANS_SERIF
        set4B.visibility = View.INVISIBLE

        set5B.text = "0"
        set5B.setTextColor(Color.parseColor("#E9ECF5"))
        set5B.typeface = Typeface.SANS_SERIF
        set5B.visibility = View.INVISIBLE

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
        scoreHistory.clear()
        addPointToHistory() // initial point
        binding.iconUndo.visibility = View.VISIBLE

        minScoreToWinGame = 4

        Toast.makeText(requireActivity(), "Match restarted", Toast.LENGTH_SHORT).show()
    }

    private fun addPointToHistory(){ // Match log point by point
        currentPoint = "$currentSet ${set1A.text} ${set1B.text} ${set2A.text} ${set2B.text} " +
                "${set3A.text} ${set3B.text} ${set4A.text} ${set4B.text} ${set5A.text} " +
                "${set5B.text} ${pointsA.text} ${pointsB.text} $servingA"
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
