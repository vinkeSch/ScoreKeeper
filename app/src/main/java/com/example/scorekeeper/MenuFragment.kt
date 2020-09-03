package com.example.scorekeeper

import android.content.res.ColorStateList
import android.graphics.Color
import android.media.Image
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

/*// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"*/

/*
*
 * A simple [Fragment] subclass.
 * Use the [MainMenuFragment.newInstance] factory method to
 * create an instance of this fragment.
*/
class MainMenuFragment : Fragment() {
/*    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null*/

    private lateinit var radioA : RadioButton
    private lateinit var radioB : RadioButton
    var servingA = false

    private lateinit var nameA : TextView
    private lateinit var nameB : TextView
    lateinit var logoTennis : ToggleButton
    lateinit var logoSpike : ToggleButton

    lateinit var nameLayout : LinearLayout

/*    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }*/

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

                    val fragment: Fragment = if (logoTennis.isChecked) MatchFragment()
                    else SpikeFragment()
                    fragment.arguments = args

                    // Navigate to the next Fragment.
                    (activity as NavigationHost).navigateTo(fragment, false)
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
        }
        logoSpike.setOnClickListener {
            logoTennis.isChecked = !logoSpike.isChecked
            nameLayout.visibility = View.VISIBLE
        }

        nameLayout = view.findViewById(R.id.nameLayout)
        nameLayout.visibility = View.INVISIBLE

        return view
    }
/*
    companion object {
        **
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainMenuFragment.

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainMenuFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }*/
}