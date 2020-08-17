package com.example.scorekeeper

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


class MainActivity :  AppCompatActivity(), NavigationHost {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, MatchFragment())
                .commit()
        }
/*
        // Initialization
        val set1A = findViewById<TextView>(R.id.textSet1A)
        val set2A = findViewById<TextView>(R.id.textSet2A)
        val set3A = findViewById<TextView>(R.id.textSet3A)

        val set1B = findViewById<TextView>(R.id.textSet1B)
        val set2B = findViewById<TextView>(R.id.textSet2B)
        val set3B = findViewById<TextView>(R.id.textSet3B)

        set2A.visibility = View.INVISIBLE
        set2B.visibility = View.INVISIBLE
        set3A.visibility = View.INVISIBLE
        set3B.visibility = View.INVISIBLE

        var ballA = findViewById<ImageView>(R.id.ballA)
        var ballB = findViewById<ImageView>(R.id.ballB)
        ballB.visibility= View.INVISIBLE


        // get reference to textview
        val pointsA = findViewById<TextView>(R.id.textPointA)
        val pointsB = findViewById<TextView>(R.id.textPointB)
        pointsA.text = "0"
        pointsB.text = "0"

        val playerNameA = findViewById<TextView>(R.id.textPlayerA)
        val playerA = Player("Federer", pointsA, set1A, set2A, set3A, ballA)
        playerNameA.text = playerA.name
        playerA.serving = true // Player A start serving; then it should be changed when a game ends
        set1A.text = playerA.setScore.toString()


        val playerB = Player("Nadal", pointsB, set1B, set2B, set3B, ballB)
        val playerNameB = findViewById<TextView>(R.id.textPlayerB)
        playerNameB.text = playerB.name
        set1B.text = playerB.setScore.toString()

        val match = Match(playerA, playerB)
        val scores = listOf("0", "15", "30", "40")

        var iconRestart = findViewById<ImageView>(R.id.imageLoop)
        iconRestart.setOnClickListener {
            Toast.makeText(this@MainActivity, "Long press to restart match", Toast.LENGTH_SHORT).show()
        }

        iconRestart.setOnLongClickListener{
            match.restartMatch()
            Toast.makeText(this, "Match restarted", Toast.LENGTH_SHORT).show()
            true
        }


        // set on-click listener
        pointsA.setOnClickListener {
            // your code to perform when the user clicks on the TextView
            //Toast.makeText(this@MainActivity, "You clicked on TextView 'Click Me'.", Toast.LENGTH_SHORT).show()
*//*            when (pointA.text) {
                "0" -> pointA.text = 15.toString()
                "15" -> pointA.text = 30.toString()
                "30" -> pointA.text = 40.toString()
                "40" -> {pointA.text = 0.toString()
                Toast.makeText(this@MainActivity, "Game Player A", Toast.LENGTH_SHORT).show()
                    val gameA = set1A.text.toString().toInt()

                    set2A.visibility = View.INVISIBLE
                    set2B.visibility = View.INVISIBLE
                    set3A.visibility = View.INVISIBLE
                    set3B.visibility = View.INVISIBLE

                    if ((gameA  < 5)) set1A.text = (gameA + 1).toString()
                    else {
                        set1A.text = "6"
                        set1A.setTextColor(resources.getColor(R.color.colorA))
                        set1A.typeface = Typeface.DEFAULT_BOLD

                        set2A.visibility = View.VISIBLE
                        set2B.visibility = View.VISIBLE
                    }
                }
            }*//*

            match.pointWonBy(playerA.name)
            //if(playerA.gameScore <=3) pointsA.text = scores[playerA.gameScore]

        }



        pointsB.setOnClickListener {
            match.pointWonBy(playerB.name)
            //if(playerB.gameScore <=3) pointsB.text = scores[playerB.gameScore]
        }




        // get reference to textview
        val volume = findViewById<ImageView>(R.id.imageVolume)
        var mute = false
        // set on-click listener
        volume.setOnClickListener {
            // your code to perform when the user clicks on the TextView
            //Toast.makeText(this@MainActivity, "You clicked on TextView 'Click Me'.", Toast.LENGTH_SHORT).show()
            if (!mute) volume.setBackgroundResource(R.drawable.ic_baseline_volume_off_30)
            else volume.setBackgroundResource(R.drawable.ic_baseline_volume_up_30)
            mute = !mute
        }*/

    }

    /**
     * Navigate to the given fragment.
     *
     * @param fragment       Fragment to navigate to.
     * @param addToBackstack Whether or not the current fragment should be added to the backstack.
     */
    override fun navigateTo(fragment: Fragment, addToBackstack: Boolean) {
        val transaction = supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)

        if (addToBackstack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }


}