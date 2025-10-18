package com.zero.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private lateinit var hintText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hintText = findViewById(R.id.hintText)
        tts = TextToSpeech(this, this)

        // start foreground voice service to keep assistant active
        val svc = Intent(this, VoiceService::class.java)
        ContextCompat.startForegroundService(this, svc)

        // Start wake word service as well
        val wake = Intent(this, WakeWordService::class.java)
        ContextCompat.startForegroundService(this, wake)

        val glow = findViewById<View>(R.id.glowCircle)
        glow.setOnClickListener { startVoiceCapture() }
        glow.setOnLongClickListener {
            // open settings
            val i = Intent(this, SettingsActivity::class.java)
            startActivity(i)
            true
        }

        // load prefs
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val savedName = prefs.getString("assist_name", "Zero") ?: "Zero"
        hintText.text = "Say \"Hey $savedName\" or tap the circle"

        // Request audio permission if needed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 101)
        }
    }

    private fun startVoiceCapture() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        startActivityForResult(intent, 102)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 102 && data != null) {
            val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = results?.get(0) ?: ""
            handleUserInput(spoken)
        }
    }

    private fun handleUserInput(text: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val savedName = prefs.getString("assist_name", "Zero") ?: "Zero"
        hintText.text = "Heard: "${text}""
        // Check connectivity
        if (isOnline()) {
            // call OpenAI online
            OpenAIClient.getAssistantReply(text, listOf()) { reply ->
                runOnUiThread {
                    speak(reply)
                    hintText.text = reply
                }
            }
        } else {
            val response = OfflineResponder.getResponse(text)
            speak(response)
            hintText.text = response
        }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "zero_reply")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.UK
            // do not auto-greet; silent until user speaks
        }
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(ConnectivityManager::class.java) ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}
