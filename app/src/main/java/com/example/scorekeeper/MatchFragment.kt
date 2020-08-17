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

/**
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

        return view
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

        matchScore = ""
    }




/*    companion object {
        *//**
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
