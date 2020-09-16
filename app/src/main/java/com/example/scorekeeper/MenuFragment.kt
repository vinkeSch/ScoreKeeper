package com.example.scorekeeper

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout

class MainMenuFragment : Fragment() {

    private lateinit var radioA : RadioButton
    private lateinit var radioB : RadioButton
    var servingA = false

    private lateinit var radioGroupSets : RadioGroup
    private lateinit var radio1 : RadioButton
    private lateinit var radio3 : RadioButton
    private lateinit var radio5 : RadioButton
    private var numberOfSets : Int = 0

    private lateinit var nameA : TextView
    private lateinit var nameB : TextView
    lateinit var logoTennis : ToggleButton
    lateinit var logoSpike : ToggleButton

    lateinit var nameLayout : ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_menu, container, false)

        val fab: View = view.findViewById(R.id.fab)
        nameA = view.findViewById(R.id.nameA)
        nameB = view.findViewById(R.id.nameB)

        fab.setOnClickListener {
            if (logoTennis.isChecked || logoSpike.isChecked) {
                if (radioA.isChecked || radioB.isChecked) {
                    // Pass data to fragment
                    val args = Bundle()
                    // Send string data as key value format
                    args.putBoolean("serve",servingA)
                    if (nameA.text.isNullOrEmpty()) args.putString("nameA", "Player A")
                    else args.putString("nameA", nameA.text.toString())
                    if (nameB.text.isNullOrEmpty()) args.putString("nameB", "Player B")
                    else args.putString("nameB", nameB.text.toString())
                    args.putInt("sets", numberOfSets)

                    val fragment: Fragment = if (logoTennis.isChecked) MatchFragment()
                    else SpikeFragment()
                    fragment.arguments = args

                    // Navigate to the next Fragment.
                    (activity as NavigationHost).navigateTo(fragment, true)
                }
                else {
                    Toast.makeText(requireActivity(), "Please select the first player to serve",
                        Toast.LENGTH_LONG).show()
                }
            }
            else Toast.makeText(requireActivity(), "Please select a sport to play",
                Toast.LENGTH_LONG).show()
        }
        fab.visibility = View.INVISIBLE

        radioA = view.findViewById(R.id.radioButtonA)
        radioB = view.findViewById(R.id.radioButtonB)

        radioA.setOnClickListener {
            if (radioA.isChecked) {
                servingA = true
                fab.backgroundTintList = ColorStateList.valueOf(Color
                        .parseColor("#99B2DD"))
                fab.visibility = View.VISIBLE
            }
        }

        radioB.setOnClickListener {
            if (radioB.isChecked) {
                servingA = false
                fab.backgroundTintList = ColorStateList.valueOf(Color
                    .parseColor("#E9AFA3"))
                fab.visibility = View.VISIBLE
            }
        }

        logoTennis = view.findViewById(R.id.logoTennis)
        logoSpike = view.findViewById(R.id.logoSpike)

        logoTennis.setOnClickListener {
            logoSpike.isChecked = !logoTennis.isChecked
            nameLayout.visibility = View.VISIBLE
            radioGroupSets.visibility = View.VISIBLE
        }
        logoSpike.setOnClickListener {
            logoTennis.isChecked = !logoSpike.isChecked
            nameLayout.visibility = View.VISIBLE
            radioGroupSets.visibility = View.VISIBLE
        }

        nameLayout = view.findViewById(R.id.nameLayout)
        nameLayout.visibility = View.INVISIBLE

        radioGroupSets = view.findViewById(R.id.radioGroupSets)
        radioGroupSets.visibility = View.INVISIBLE

        numberOfSets = 3 // By default 3 sets are played
        radio1 = view.findViewById(R.id.radio1Set)
        radio3 = view.findViewById(R.id.radio3Set)
        radio5 = view.findViewById(R.id.radio5Set)

        radio1.setOnClickListener {
            if (radio1.isChecked) {
                numberOfSets = 1
            }
        }

        radio3.setOnClickListener {
            if (radio3.isChecked) {
                numberOfSets = 3
            }
        }

        radio5.setOnClickListener {
            if (radio5.isChecked) {
                numberOfSets = 5
            }
        }

        return view
    }
}