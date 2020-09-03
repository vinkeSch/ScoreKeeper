package com.example.scorekeeper

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_match.*
import java.util.*
import kotlin.math.abs


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
/*
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
*/

/*
 * A simple [Fragment] subclass.
 * Use the [MatchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MatchFragment : Fragment(), TextToSpeech.OnInitListener  {
/*    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }*/

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
    private  lateinit var set2B: TextView
    private lateinit var set3B: TextView

    private lateinit var ballA: ImageView
    private lateinit var ballB: ImageView
    private lateinit var speech: ImageView

    private var currentPoint = ""
    private  var pointNumber = 0
    private  var scoreHistory = mutableListOf<String>()

    private var tts: TextToSpeech? = null

    private var tiebreakPointNumber = 0
    private var mute = false

    private val TAG = "MatchFragment"

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_match, container, false)

        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener(View.OnKeyListener { _, keyCode, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    Log.d(TAG, "Enter button was pressed")
                    pointWonByB()
                    return@OnKeyListener true
                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    Log.d(TAG, "Volume up button was pressed")
                    pointWonByA()
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

        set2A.visibility = View.INVISIBLE
        set2B.visibility = View.INVISIBLE
        set3A.visibility = View.INVISIBLE
        set3B.visibility = View.INVISIBLE

        ballA = view.findViewById(R.id.ballA)
        ballB = view.findViewById(R.id.ballB)
        ballB.visibility= View.INVISIBLE

        speech = view.findViewById(R.id.imageVolume)
        speech.visibility = View.INVISIBLE // enable visibility when voice engine is ready
        tts = TextToSpeech(requireActivity(), this)

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

        scoreVoiceMap = mapOf(
            "0-15" to "Love fifteen", "0-30" to "Love thirty",
            "0-40" to "Love forty", "15-0" to "Fifteen love", "30-0" to "Thirty love",
            "40-0" to "Forty love", "15-40" to "Fifteen forty", "15-15" to "Fifteen all",
            "30-15" to "Thirty fifteen", "15-30" to "Fifteen thirty", "30-30" to "Thirty all",
            "40-30" to "Forty thirty", "30-40" to "Thirty forty",
            "40-15" to "Forty fifteen"
        )

        // Reset match
        val iconRestart = view.findViewById<ImageView>(R.id.imageLoop)
        iconRestart.setOnClickListener {
            Toast.makeText(requireActivity(), "Long press to restart match", Toast.LENGTH_SHORT).show()
        }

        iconRestart.setOnLongClickListener{
            resetMatch()
            Toast.makeText(requireActivity(), "Match restarted", Toast.LENGTH_SHORT).show()
            true
        }

        // Undo last point
        val iconUndo = view.findViewById<ImageView>(R.id.imageUndo)
        iconUndo.setOnClickListener {
            if (pointNumber == 1) Toast.makeText(
                requireActivity(),
                "First point of the match!\nLet's play!",
                Toast.LENGTH_SHORT
            ).show()
            else getPreviousScore()
        }

        pointsA.setOnClickListener {
            pointWonByA()
          }

        pointsB.setOnClickListener {
            pointWonByB()
        }

        speech.setOnClickListener {
            if (!mute) speech.setBackgroundResource(R.drawable.ic_baseline_volume_off_30)
            else speech.setBackgroundResource(R.drawable.ic_baseline_volume_up_30)
            mute = !mute
        }

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
            else -> { // 3rd set
                currentSet =3
                setScoreA = scoreValues[5].toInt()
                setScoreB = scoreValues[6].toInt()
            }
        }

        if(!(setScoreA == 6 && setScoreB == 6 )){ // no tiebreak
            if ((scoreValues[7] != "AD") && (scoreValues[8] != "AD") ) {
                gameScoreA = scoreMap[scoreValues[7]] ?: error("")
                gameScoreB = scoreMap[scoreValues[8]] ?: error("")
            }
            else if (scoreValues[7] == "AD") {
                gameScoreA = 5
                gameScoreB = 4
            }
            else if (scoreValues[8] == "AD") {
                gameScoreA = 4
                gameScoreB = 5
            }
        }
        else { // tiebreak
            tiebreakPointNumber--
            gameScoreA = scoreValues[7].toInt()
            gameScoreB = scoreValues[8].toInt()
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
                        speakOut("Deuce")
                    }
                    -1 -> {
                        pointsB.text = "AD"
                        speakOut("Advantage ${playerNameB.text}")
                    }
                    1 -> {
                        pointsA.text = "AD"
                        speakOut("Advantage ${playerNameA.text}")
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
            when {
                (gameScoreA > gameScoreB) -> speakOut("$gameScoreA $gameScoreB. ${playerNameA.text}")
                (gameScoreA < gameScoreB) -> speakOut("$gameScoreB $gameScoreA. ${playerNameB.text}")
                else -> speakOut("$gameScoreB all")
            }
        }

        val diff = gameScoreA - gameScoreB
        if ( diff > 1 && gameScoreA >= minScoreToWinGame ) {
            gameWonByA()
            // check if the set has been won
            val matchScoreIsClose = abs(setScoreA - setScoreB) < 2

            if (setScoreA == 7 || (!matchScoreIsClose && setScoreA >= 6)) { // won the set
                    setWonByA()
                    if(setsWonA == 2){ // MATCH WON
                        matchWonByA()
                        matchByVoice()
                    }
                    else { // NEXT SET
                        // set to voice
                        speakOut("Game and Set ${playerNameA.text}. $setOldA $setOldB")
                        nextSet()
                    }
            }
            else { // GAME WON; NOT THE SET
                speakOut("Game ${playerNameA.text},")
                tts?.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, null)
                gameScoreToVoice()
            }
        }
        else{ // POINT WON; NOT THE GAME
            if (servingA) scoreVoiceMap["${pointsA.text}-${pointsB.text}"]?.let { speakOut(it) }
            else scoreVoiceMap["${pointsB.text}-${pointsA.text}"]?.let { speakOut(it) }
        }

        addPointToHistory() //update the score history
    }

    private fun gameScoreToVoice() { // Score in games to speech
        if ( setScoreA == 6 && setScoreB == 6 ) {
            isTiebreak = true
            tiebreakPointNumber = 1
            minScoreToWinGame = 7
            speakOutAdd("Six games all. Tie break")
        }
        else{
            when{
                (setScoreA > setScoreB) -> {
                    if (setScoreA > 1) speakOutAdd("${playerNameA.text} leads $setScoreA games to $setScoreB")
                    else speakOutAdd("${playerNameA.text} leads $setScoreA game to $setScoreB")
                }
                (setScoreA < setScoreB) -> {
                    if (setScoreB > 1) speakOutAdd("${playerNameB.text} leads $setScoreB games to $setScoreA")
                    else speakOutAdd("${playerNameB.text} leads $setScoreB game to $setScoreA")
                }
                else -> {
                    if (setScoreA > 1) speakOutAdd("$setScoreA games all")
                    else speakOutAdd("$setScoreA game all")
                }
            }
        }
    }

    private fun matchByVoice() {
        if(setsWonA == 2){ // Player A won
            when (currentSet){
                2 -> speakOut("Game, set and match ${playerNameA.text}. ${set1A.text} ${set1B.text}, ${set2A.text} ${set2B.text}")
                3 -> speakOut("Game, set and match ${playerNameA.text}. ${set1A.text} ${set1B.text}, ${set2A.text} ${set2B.text}, ${set3A.text} ${set3B.text}")
            }
        }
        else { // Player B won
            when (currentSet){
                2 -> speakOut("Game, set and match ${playerNameB.text}. ${set1B.text} ${set1A.text}, ${set2B.text} ${set2A.text}")
                3 -> speakOut("Game, set and match ${playerNameB.text}. ${set1B.text} ${set1A.text}, ${set2B.text} ${set2A.text}, ${set3B.text} ${set3A.text}")
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
        imageUndo.visibility = View.INVISIBLE
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
                        speakOut("Deuce")
                    }
                    -1 -> {
                        pointsB.text = "AD"
                        speakOut("Advantage ${playerNameB.text}")
                    }
                    1 -> {
                        pointsA.text = "AD"
                        speakOut("Advantage ${playerNameA.text}")
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
            when {
                (gameScoreA > gameScoreB) -> speakOut("$gameScoreA $gameScoreB. ${playerNameA.text}")
                (gameScoreA < gameScoreB) -> speakOut("$gameScoreB $gameScoreA. ${playerNameB.text}")
                else -> speakOut("$gameScoreB all")
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
                    if(setsWonB == 2){ // MATCH WON
                        matchWonByB()
                        matchByVoice()
                    }
                    else { // NEXT SET
                        // set to voice
                        speakOut("Game and Set ${playerNameB.text}. $setOldB $setOldA")
                        nextSet()
                    }

            }
            else { // GAME WON; NOT THE SET
                speakOut("Game ${playerNameB.text}.")
                tts?.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, null)
                gameScoreToVoice()
            }
        }
        else{ // POINT WON; NOT THE SET
            if (servingA) scoreVoiceMap["${pointsA.text}-${pointsB.text}"]?.let { speakOut(it) }
            else scoreVoiceMap["${pointsB.text}-${pointsA.text}"]?.let { speakOut(it) }
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
        imageUndo.visibility = View.INVISIBLE
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
        imageUndo.visibility = View.VISIBLE

        minScoreToWinGame = 4
    }

    private fun addPointToHistory(){
        currentPoint = "$currentSet ${set1A.text} ${set1B.text} ${set2A.text} ${set2B.text} ${set3A.text} ${set3B.text} ${pointsA.text} ${pointsB.text} $servingA"
        scoreHistory.add(pointNumber, currentPoint)
        pointNumber++
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            //val localeES = Locale("es", "ES")
            val localeUS = Locale.US
            //val result: Int
            //result = tts?.setLanguage(localeES)!!
            val result = tts?.setLanguage(localeUS) // set US English as language for tts

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(activity, "This Language is not supported", Toast.LENGTH_SHORT).show()
                //tts?.language = localeUS
            } else {
                // enable voice button
                imageVolume.visibility = View.VISIBLE
            }
        } else {
            Toast.makeText(activity, "Initialization Failed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speakOut(message: String) {
        if(!mute) tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun speakOutAdd(message: String) {
        if(!mute) tts?.speak(message, TextToSpeech.QUEUE_ADD, null, null)
    }

    override fun onDestroy() {
        // Shutdown TTS
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onDestroy()
    }

/*    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainMenuFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MatchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }*/
}
