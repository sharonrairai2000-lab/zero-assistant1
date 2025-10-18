package com.zero.assistant

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        val assistName: EditText = findViewById(R.id.assistName)
        val apiKey: EditText = findViewById(R.id.apiKey)
        val voiceTone: Spinner = findViewById(R.id.voiceTone)
        val responseMode: Spinner = findViewById(R.id.responseMode)
        val saveBtn: Button = findViewById(R.id.saveBtn)

        assistName.setText(prefs.getString("assist_name", "Zero"))
        apiKey.setText(prefs.getString("openai_key", ""))

        voiceTone.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Calm","Energetic","Formal"))
        responseMode.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Always aloud","Text only"))

        saveBtn.setOnClickListener {
            val e = prefs.edit()
            e.putString("assist_name", assistName.text.toString())
            e.putString("openai_key", apiKey.text.toString())
            e.putString("voice_tone", voiceTone.selectedItem.toString())
            e.putString("response_mode", responseMode.selectedItem.toString())
            e.apply()
            finish()
        }
    }
}
