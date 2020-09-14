package com.example.scorekeeper

import android.app.Activity
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.*

class TTS(private val activity: Activity,
          private val es: Boolean) : TextToSpeech.OnInitListener {

    val tts: TextToSpeech = TextToSpeech(activity, this)

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {

            // val localeBR = Locale("pt", "BR")
            val localeES = Locale("es", "MX")
            val localeUS = Locale.US

            val result: Int
            result = if (es) tts.setLanguage(localeES) else tts.setLanguage(localeUS)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(activity, "This Language is not supported", Toast.LENGTH_SHORT).show()
            } else {
                // enable voice button
                // speakOut(message)
            }

        } else {
            Toast.makeText(activity, "Initialization Failed!", Toast.LENGTH_SHORT).show()
        }
    }

    fun speakOut(message: String) {
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun speakOutAdd(message: String) {
        tts.speak(message, TextToSpeech.QUEUE_ADD, null, null)
    }
}