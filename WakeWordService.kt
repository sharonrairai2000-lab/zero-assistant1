package com.zero.assistant

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class WakeWordService : Service() {
    private var sr: SpeechRecognizer? = null

    override fun onCreate() {
        super.onCreate()
        startListening()
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return
        sr = SpeechRecognizer.createSpeechRecognizer(this)
        sr?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { restart() }
            override fun onError(error: Int) { restart() }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.joinToString(" ")?.lowercase() ?: ""
                Log.d("ZeroWake", "Heard: $text")
                if (text.contains("hey zero")) {
                    val i = Intent(this@WakeWordService, MainActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(i)
                }
                restart()
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        sr?.startListening(intent)
    }

    private fun restart() {
        sr?.stopListening()
        sr?.cancel()
        startListening()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        sr?.destroy()
        super.onDestroy()
    }
}
