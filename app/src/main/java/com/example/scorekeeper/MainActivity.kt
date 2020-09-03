package com.example.scorekeeper

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.io.IOException
import java.util.*

class MainActivity :  AppCompatActivity(), NavigationHost {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mBluetoothAdapter= BluetoothAdapter.getDefaultAdapter()

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, MainMenuFragment())
                .commit()
        }
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
            setupConn()
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
}