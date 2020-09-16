package com.example.scorekeeper

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.welcome_slide1.*
import kotlinx.android.synthetic.main.welcome_slide2.*
import kotlinx.android.synthetic.main.welcome_slide3.*
import kotlinx.android.synthetic.main.welcome_slide4.*

lateinit var layouts: IntArray

class MainActivity :  AppCompatActivity(), NavigationHost {

    private var backPressedTime:Long = 0
    private lateinit var backToast:Toast

    private val TAG = "MainActivity"
    private val REQUEST_ENABLE_BT = 1

    //Representa el adaptador local de Bluetooth (radio Bluetooth). El BluetoothAdapter es el punto
    // de entrada de toda interacción de Bluetooth. Gracias a esto, puedes ver otros dispositivos
    // Bluetooth, consultar una lista de los dispositivos conectados (sincronizados), crear una
    // instancia de BluetoothDevice mediante una dirección MAC conocida y crear un
    // BluetoothServerSocket para recibir comunicaciones de otros dispositivos.

    // Local Bluetooth adapter
    private var mBluetoothAdapter: BluetoothAdapter? = null

    // Member object for the bluetooth services
    private var mBluetoothService: BluetoothService? = null

    private var myViewPagerAdapter: MyViewPagerAdapter? = null
    private lateinit var dotsLayout: LinearLayout
    private var numberOfSets : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //viewPager = findViewById(R.id.view_pager)
        //dotsLayout = findViewById(R.id.layoutDots)
        //btnSkip = findViewById(R.id.btn_skip)
       // btnNext = findViewById(R.id.btn_next)
        // layouts of all welcome sliders
        // add few more layouts if you want
        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = intArrayOf(
            R.layout.welcome_slide1,
            R.layout.welcome_slide2,
            R.layout.welcome_slide3,
            R.layout.welcome_slide4
        )
        dotsLayout = findViewById(R.id.layoutDots)
        addBottomDots(0) // adding bottom dots

        myViewPagerAdapter = MyViewPagerAdapter()
        view_pager.offscreenPageLimit = 3 // "off screen" pages to keep loaded each side of the current page
        view_pager.adapter = myViewPagerAdapter
        view_pager.addOnPageChangeListener(viewPagerPageChangeListener)

        btn_skip.setOnClickListener {
            launchMatchScreen(savedInstanceState)
        }

        btn_next.setOnClickListener {
            // checking for last page
            // if last page match screen will be launched
            val current: Int = getItem(+1)
            if (current < layouts.size) {
                // move to next screen
                view_pager.currentItem = current
            } else {
                if(!btn1.isChecked && !btn3.isChecked && !btn5.isChecked) { // no sets selected
                    Toast.makeText(this, "Shall we play 1, 3 or 5 sets?",
                        Toast.LENGTH_LONG).show()
                    view_pager.currentItem = 2 // sets selection page
                }
                else launchMatchScreen(savedInstanceState)
            }
        }

        mBluetoothAdapter= BluetoothAdapter.getDefaultAdapter()
    }

    private fun addBottomDots(currentPage: Int) {
        val dots = arrayOfNulls<TextView>(layouts.size)
        val colorsActive = resources.getIntArray(R.array.array_dot_active)
        val colorsInactive = resources.getIntArray(R.array.array_dot_inactive)
        dotsLayout.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i]!!.text = HtmlCompat.fromHtml("&#8226;", HtmlCompat.FROM_HTML_MODE_LEGACY)
            dots[i]!!.textSize = 35F
            dots[i]!!.setTextColor(colorsInactive[currentPage])
            dotsLayout.addView(dots[i])
        }
        if (dots.isNotEmpty()) dots[currentPage]!!.setTextColor(colorsActive[currentPage])
    }

    override fun onStart() {
        super.onStart()
        if (mBluetoothAdapter == null) {
            return
        }
        // If BT is not on, request that it be enabled.
        // setupConn() will then be called during onActivityResult
        if (!mBluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
            // Otherwise, setup the connection session
        } else if (mBluetoothService == null) {
            //setupConn()
        }
    }

    private fun setupConn() {
        Log.i(TAG, "setupConn")

        // Para inicializar una conexión con el dispositivo remoto que acepta conexiones en un
        // socket de servidor abierto, primero debes obtener un objeto BluetoothDevice que
        // represente al dispositivo remoto. -> AB Shutter3: FF:FF:FF:FF:FF:FF
        val device: BluetoothDevice? = mBluetoothAdapter?.getRemoteDevice("FF:FF:FF:FF:FF:FF")

        // Initialize the BluetoothChatService to perform bluetooth connections
        mBluetoothService = device?.let { BluetoothService(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                if (mBluetoothAdapter?.isEnabled!!) {
                    Toast.makeText(
                        this, "Bluetooth enabled",
                        Toast.LENGTH_SHORT
                    ).show()
                    /**
                     * Antes de llevar a cabo la detección de dispositivos, es importante
                     * consultar el conjunto de dispositivos sincronizados a fin de ver si
                     * el dispositivo deseado ya es conocido. Para ello, llama a
                     * getBondedDevices(). Esto devuelve un conjunto de objetos BluetoothDevice
                     * que representa a los dispositivos sincronizados. Por ejemplo, puedes
                     * consultar todos los dispositivos sincronizados y obtener el nombre y
                     * la dirección MAC de cada uno, como se demuestra en el siguiente fragmento
                     * de código:
                     * Por ejemplo:
                     * Galaxy Buds (19E1): F4:7D:EF:05:19:E1
                     * REIVAX SPEAKER: 88:C6:26:1B:1B:1E
                     * AB Shutter3: FF:FF:FF:FF:FF:FF
                     */
                    val pairedDevices: Set<BluetoothDevice>? = mBluetoothAdapter?.bondedDevices
                    pairedDevices?.forEach { device ->
                        println("DEVICE: ${device.name}: ${device.address}")
                    }
                } else {
                    Toast.makeText(
                        this, "Bluetooth disabled",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(
                    this, "Bluetooth enabling has been canceled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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

    override fun onDestroy() {
        super.onDestroy()
        mBluetoothService?.stop()
    }

    override fun onBackPressed() {
        backToast = Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT)
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel()
            super.onBackPressed()
            return
        } else {
            backToast.show()
        }
        backPressedTime = System.currentTimeMillis()
    }

    private fun getItem(i: Int): Int {
        return view_pager.currentItem + i
    }

    private fun getNumberOfSets() : Int {
        if (btn1.isChecked) numberOfSets = 1
        if (btn3.isChecked) numberOfSets = 3
        if (btn5.isChecked) numberOfSets = 5
        return numberOfSets
    }

    private fun launchMatchScreen(savedInstanceState: Bundle?) {

        // Pass data to fragment
        val args = Bundle()
        // Send string data as key value format
        args.putBoolean("serve",false)
        if (nameA.text.isNullOrEmpty()) args.putString("nameA", "Player A")
        else args.putString("nameA", nameA.text.toString())
        if (nameB.text.isNullOrEmpty()) args.putString("nameB", "Player B")
        else args.putString("nameB", nameB.text.toString())
        args.putInt("sets", getNumberOfSets())

        val fragment: Fragment = MatchFragment()
        fragment.arguments = args

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, fragment)
                .commit()
        }
    }

    //  viewpager change listener
    private var viewPagerPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            addBottomDots(position)
            when(position){
                2 -> { // Number of sets selection
                    btn_next.visibility = View.VISIBLE
                    btn_next.setImageResource(R.drawable.ic_round_keyboard_arrow_right_24)
                    btn_next.setBackgroundResource(R.drawable.rounded_corners_btn_next)
                    btn1.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) { // The toggle is enabled, disable the others
                            btn3.isChecked = false
                            btn5.isChecked = false
                        }
                    }
                    btn3.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            btn1.isChecked = false
                            btn5.isChecked = false
                        }
                    }
                    btn5.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            btn1.isChecked = false
                            btn3.isChecked = false
                        }
                    }
                }
                3 -> { // 1st player to serve
                    if(!btnA.isChecked && !btnB.isChecked) btn_next.visibility = View.GONE
                    if(btnA.isChecked) {
                        btn_next.setImageResource(R.drawable.round_sports_baseball_white_24)
                        btn_next.setBackgroundResource(R.drawable.rounded_corners_btn_next_a)
                    }
                    if(btnB.isChecked) {
                        btn_next.setImageResource(R.drawable.round_sports_baseball_white_24)
                        btn_next.setBackgroundResource(R.drawable.rounded_corners_btn_next_b)
                    }
                    // get names from previous pages
                    if (!nameA.text.isNullOrEmpty()) {
                        btnA.text = nameA.text
                        btnA.textOff = nameA.text
                        btnA.textOn = nameA.text
                    }
                    if (!nameB.text.isNullOrEmpty()) {
                        btnB.text = nameB.text
                        btnB.textOff = nameB.text
                        btnB.textOn = nameB.text
                    }

                    btnA.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) { // Player A first to serve
                            btnB.isChecked = false
                            btn_next.visibility = View.VISIBLE
                            btn_next.setImageResource(R.drawable.round_sports_baseball_white_24)
                            btn_next.setBackgroundResource(R.drawable.rounded_corners_btn_next_a)
                        }
                        else {
                            btn_next.visibility = View.GONE
                            btn_next.setImageResource(R.drawable.ic_round_keyboard_arrow_right_24)
                            btn_next.setBackgroundResource(R.drawable.rounded_corners_btn_next)
                        }
                    }
                    btnB.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) { // Player B first to serve
                            btnA.isChecked = false
                            btn_next.visibility = View.VISIBLE
                            btn_next.setImageResource(R.drawable.round_sports_baseball_white_24)
                            btn_next.setBackgroundResource(R.drawable.rounded_corners_btn_next_b)
                        }
                        else {
                            btn_next.visibility = View.GONE
                            btn_next.setImageResource(R.drawable.ic_round_keyboard_arrow_right_24)
                            btn_next.setBackgroundResource(R.drawable.rounded_corners_btn_next)
                        }
                    }
                }
            }
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    /**
     * View pager adapter
     */
    class MyViewPagerAdapter : PagerAdapter() {

        private var layoutInflater: LayoutInflater? = null
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            layoutInflater = LayoutInflater.from(container.context)
            val view: View = layoutInflater!!.inflate(layouts[position], container, false)
            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return layouts.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view: View = `object` as View
            container.removeView(view)
        }
    }
}