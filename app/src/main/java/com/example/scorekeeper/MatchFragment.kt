package com.example.scorekeeper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_match.*
import kotlin.math.abs

/*
// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"*/

/*
 * A simple [Fragment] subclass.
 * Use the [MatchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MatchFragment : Fragment() {
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
    val scores = listOf("0", "15", "30", "40")
    val scoreMap = mapOf("0" to 0, "15" to 1, "30" to 2, "40" to 3)
    var matchScore = ""

    var gameScoreA = 0
    var gameScoreB = 0
    var setScoreA = 0
    var setScoreB = 0
    var setsWonA = 0
    var setsWonB = 0

    var isTiebreak = false
    var currentSet = 1
    var servingA = false

    var minScoreToWinGame = 4

    lateinit var currentTextSetA: TextView
    lateinit var currentTextSetB: TextView

    lateinit var playerNameA: TextView
    lateinit var playerNameB: TextView

    lateinit var pointsA: TextView
    lateinit var pointsB: TextView

    lateinit var set1A: TextView
    lateinit var set2A: TextView
    lateinit var set3A: TextView
    lateinit var set1B: TextView
    lateinit var set2B: TextView
    lateinit var set3B: TextView

    lateinit var ballA: ImageView
    lateinit var ballB: ImageView

    var currentPoint = ""
    var pointNumber = 0
    var scoreHistory = mutableListOf<String>()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_match, container, false)

        set1A = view.findViewById<TextView>(R.id.textSet1A)
        set2A = view.findViewById<TextView>(R.id.textSet2A)
        set3A = view.findViewById<TextView>(R.id.textSet3A)

        set1B = view.findViewById<TextView>(R.id.textSet1B)
        set2B = view.findViewById<TextView>(R.id.textSet2B)
        set3B = view.findViewById<TextView>(R.id.textSet3B)

        set2A.visibility = View.INVISIBLE
        set2B.visibility = View.INVISIBLE
        set3A.visibility = View.INVISIBLE
        set3B.visibility = View.INVISIBLE

        ballA = view.findViewById<ImageView>(R.id.ballA)
        ballB = view.findViewById<ImageView>(R.id.ballB)
        ballB.visibility= View.INVISIBLE

        currentTextSetA = set1A
        currentTextSetB = set1B

        pointsA = view.findViewById<TextView>(R.id.textPointsA)
        pointsB = view.findViewById<TextView>(R.id.textPointsB)
        pointsA.text = "0"
        pointsB.text = "0"

        playerNameA = view.findViewById<TextView>(R.id.textPlayerA)
        playerNameA.text = "Federer"
        servingA = true // Player A start serving; then it should be changed when a game ends

        playerNameB = view.findViewById<TextView>(R.id.textPlayerB)
        playerNameB.text = "Nadal"

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
            if (pointNumber == 1) Toast.makeText(requireActivity(), "First point of the match!\nLet's play!", Toast.LENGTH_SHORT).show()
            else getPreviousScore()
        }

        pointsA.setOnClickListener {
            pointWonByA()
          }

        pointsB.setOnClickListener {
            pointWonByB()
        }

        val volume = view.findViewById<ImageView>(R.id.imageVolume)
        var mute = false

        volume.setOnClickListener {
            if (!mute) volume.setBackgroundResource(R.drawable.ic_baseline_volume_off_30)
            else volume.setBackgroundResource(R.drawable.ic_baseline_volume_up_30)
            mute = !mute
        }

        // Initial point
        addPointToHistory()

        return view
    }

    fun getPreviousScore(){
        val scoreValues = (scoreHistory[pointNumber-2].split(" "))
        println(scoreValues)

        when(scoreValues[0]){ // number of set playing
            "1" -> { // 1st set
                if(setsWonA == 1) {
                    currentSet--
                    setsWonA = 0
                    set1A.setTextColor(Color.parseColor("#E9ECF5"))
                    set1A.typeface = Typeface.SANS_SERIF
                    currentTextSetA = set1A
                    currentTextSetB = set1B
                    matchScore = ""
                }
                else if (setsWonB == 1){
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
                val matchScoreIsClose = abs(previousSet2A - previousSet2B ) < 2
                if (previousSet2A == 7 || (!matchScoreIsClose && previousSet2A >= 6)){
                    currentSet--
                    setsWonA--
                    set2A.setTextColor(Color.parseColor("#E9ECF5"))
                    set2A.typeface = Typeface.SANS_SERIF
                    currentTextSetA = set2A
                    currentTextSetB = set2B
                    matchScore = matchScore.substring(0,3)
                }
                else if (previousSet2B == 7 || (!matchScoreIsClose && previousSet2B >= 6)){
                    currentSet--
                    setsWonB--
                    set2B.setTextColor(Color.parseColor("#E9ECF5"))
                    set2B.typeface = Typeface.SANS_SERIF
                    currentTextSetA = set2A
                    currentTextSetB = set2B
                    matchScore = matchScore.substring(0,3)
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

        if (!isTiebreak){ // no tiebreak
            if(gameScoreA <=3) pointsA.text = scores[gameScoreA]
            else { // gamescore > 3 -> deuce/adv scoring
                when (gameScoreA - gameScoreB) {
                    0 -> {
                        pointsA.text = "40"
                        pointsB.text = "40"
                    }
                    -1 -> pointsB.text = "AD"
                    1 -> pointsA.text = "AD"
                    else -> println("NADA")
                }
            }
        }
        else { // tiebreak scoring
            pointsA.text = gameScoreA.toString()
        }

        val diff = gameScoreA - gameScoreB
        if ( diff > 1 && gameScoreA >= minScoreToWinGame ) {
            gameWonByA()
            // check if the set has been won
            val matchScoreIsClose = abs(setScoreA - setScoreB ) < 2

            when {
                (setScoreA == 7 || (!matchScoreIsClose && setScoreA >= 6))-> { // won the set
                    setWonByA()
                    if(setsWonA == 2){ // won the match
                        matchWonByA()
                    }
                    else { // next set
                        nextSet()
                    }
                }
            }
        }
        if ( setScoreA == 6 && setScoreB == 6 ) {
            isTiebreak = true
            minScoreToWinGame = 7
        }

        addPointToHistory() //update the score history
    }

    fun gameWonByA() {
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

    fun setWonByA() {
        currentTextSetA.setTextColor(Color.parseColor("#99B2DD"))
        currentTextSetA.typeface = Typeface.DEFAULT_BOLD
        setsWonA++
        matchScore += "${setScoreA}-${setScoreB} "
        isTiebreak = false
        setScoreA = 0
        setScoreB = 0
        minScoreToWinGame = 4
    }

    fun matchWonByA() {
        Toast.makeText(requireActivity(), "${playerNameA.text} is the Winner \n $matchScore ", Toast.LENGTH_LONG).show()
        pointsA.visibility = View.INVISIBLE
        pointsB.visibility = View.INVISIBLE
        ballA.visibility = View.INVISIBLE
        ballB.visibility = View.INVISIBLE
        imageUndo.visibility = View.INVISIBLE
    }

    @SuppressLint("SetTextI18n")
    fun pointWonByB() {
        gameScoreB++

        if (!isTiebreak){ // no tiebreak
            if(gameScoreB <=3) pointsB.text = scores[gameScoreB]
            else { // gamescore > 3 -> deuce/adv scoring
                when (gameScoreA - gameScoreB) {
                    0 -> {
                        pointsA.text = "40"
                        pointsB.text = "40"
                    }
                    -1 -> pointsB.text = "AD"
                    1 -> pointsA.text = "AD"
                    else -> println("NADA")
                }
            }
        }
        else { // tiebreak
            pointsB.text = gameScoreB.toString()
        }

        val diff = gameScoreB - gameScoreA
        if ( diff > 1 && gameScoreB >= minScoreToWinGame ) {
            gameWonByB()
            // check if the set has been won
            val matchScoreIsClose = abs(setScoreA - setScoreB ) < 2
            when {
                (setScoreB == 7 || (!matchScoreIsClose && setScoreB >= 6))-> { // won the set
                    setWonByB()
                    if(setsWonB == 2){ // won the match
                        matchWonByB()
                    }
                    else { // next set
                        nextSet()
                    }
                }
            }
        }
        if ( setScoreA == 6 && setScoreB == 6 ) {
            isTiebreak = true
            minScoreToWinGame = 7
        }

        addPointToHistory() //update the score history
    }

    fun gameWonByB() {
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

    fun setWonByB() {
        currentTextSetB.setTextColor(Color.parseColor("#E9AFA3"))
        currentTextSetB.typeface = Typeface.DEFAULT_BOLD
        setsWonB++
        matchScore += "${setScoreA}-${setScoreB} "
        isTiebreak = false
        setScoreA = 0
        setScoreB = 0
        minScoreToWinGame = 4
    }

    fun matchWonByB() {
        Toast.makeText(requireActivity(), "${playerNameB.text} is the Winner \n $matchScore ", Toast.LENGTH_LONG).show()
        pointsA.visibility = View.INVISIBLE
        pointsB.visibility = View.INVISIBLE
        ballA.visibility = View.INVISIBLE
        ballB.visibility = View.INVISIBLE
        imageUndo.visibility = View.INVISIBLE
    }

    fun nextSet() {
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

    fun setIcon(){
        if (servingA) {
            ballA.visibility = View.VISIBLE
            ballB.visibility = View.INVISIBLE
        }
        else {
            ballB.visibility = View.VISIBLE
            ballA.visibility = View.INVISIBLE
        }
    }

    fun resetMatch() {
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
    }

    fun addPointToHistory(){
        currentPoint = "$currentSet ${set1A.text} ${set1B.text} ${set2A.text} ${set2B.text} ${set3A.text} ${set3B.text} ${pointsA.text} ${pointsB.text} $servingA"
        scoreHistory.add(pointNumber,currentPoint)
        pointNumber++
    }




/*    companion object {
        *//*
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MatchFragment.
         *//*
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
