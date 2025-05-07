package com.example.melodia

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class Optionsactivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        loadLocale()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.options_activity)
        hideSystemUI()

        val volumeButton = findViewById<TextView>(R.id.volumeButton)
        val volumeControl = findViewById<LinearLayout>(R.id.volumeControl)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val languageButton = findViewById<TextView>(R.id.languajeButton)
        val backArrow = findViewById<ImageView>(R.id.backArrow)

        // Mostrar u ocultar el control de volumen
        volumeButton.setOnClickListener {
            volumeControl.visibility = if (volumeControl.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // Configurar SeekBar para volumen
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        seekBar.max = maxVolume
        seekBar.progress = currentVolume

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Aplicar el volumen al sistema
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)

                // Guardar el valor en SharedPreferences
                val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
                prefs.edit().putInt("user_volume", progress).apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Botón de cambiar idioma
        languageButton.setOnClickListener {
            toggleLanguage()
        }

        // Flecha de regreso
        backArrow.setOnClickListener {
            finish()
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }

    private fun toggleLanguage() {
        val currentLocale = resources.configuration.locales[0].language
        val newLocale = if (currentLocale == "es") "en" else "es"

        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("App_Lang", newLocale)
            apply()
        }

        setLocale(newLocale)

        // Reiniciar actividad para aplicar idioma
        val intent = Intent(this, Optionsactivity::class.java)
        finish()
        startActivity(intent)
    }

    private fun loadLocale() {
        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val language = sharedPreferences.getString("App_Lang", "es") ?: "es"
        setLocale(language)
    }

    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}
