package com.example.scorekeeper

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.ParcelUuid
import android.util.Log
import java.io.IOException
import java.util.*

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */

class BluetoothService(device: BluetoothDevice) {

    // Debugging
    private val TAG = "BluetoothService"

    // Name for the SDP record when creating server socket
    private val NAME = "BluetoothSecure"

    // El identificador único universal (UUID) es un ID de string con un formato estandarizado de
    // 128 bits que se emplea para identificar información de manera exclusiva. Conforma la base
    // del acuerdo de conexión con el dispositivo del cliente. Es decir, cuando el cliente
    // intenta conectarse con este dispositivo, incluye un UUID que identifica de manera única el
    // servicio con el cual desea conectarse.

    // Unique UUID for this application
    private var MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")

    private val bluetoothAdapter: BluetoothAdapter? = null

    private var mmDevice = device
    private var mSecureAcceptThread: AcceptThread? = null

    private val mState = 0
    private val mNewState = 0

    // Constants that indicate the current connection state
    val STATE_NONE = 0 // we're doing nothing
    val STATE_LISTEN = 1 // now listening for incoming connections
    val STATE_CONNECTING = 2 // now initiating an outgoing connection
    val STATE_CONNECTED = 3 // now connected to a remote device


    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    @Synchronized
    fun start() {
        Log.d(TAG, "start()")

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = AcceptThread()
            mSecureAcceptThread!!.start()
        }
    }

    fun stop() {
        Log.d(TAG, "stop()")

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread!!.cancel()
            mSecureAcceptThread = null
        }
    }

    /**
     * A diferencia del protocolo TCP/IP, RFCOMM solo permite un cliente conectado por canal
     * a la vez, por lo que, en la mayoría de los casos, tiene sentido llamar a close() en el
     * BluetoothServerSocket inmediatamente después de aceptar un socket conectado.
     */
    private inner class AcceptThread () : Thread() {

        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            if(mmDevice.uuids != null) {
                val list: Array<ParcelUuid> = mmDevice.uuids
                MY_UUID = UUID.fromString(list[0].toString())
            }
            Log.i(TAG, "UUID used: $MY_UUID")
            bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID)
        }

        override fun run() {
            Log.i(TAG, "BEGIN mAcceptThread:")
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter?.cancelDiscovery()

            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }
}