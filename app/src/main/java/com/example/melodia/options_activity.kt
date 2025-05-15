package com.example.melodia

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class Optionsactivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.options_activity)
        hideSystemUI()

        prefs = getSharedPreferences("config", Context.MODE_PRIVATE)

        val volumeButton = findViewById<TextView>(R.id.volumeButton)
        val volumeControl = findViewById<LinearLayout>(R.id.volumeControl)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val languageButton = findViewById<TextView>(R.id.languajeButton)
        val backArrow = findViewById<ImageView>(R.id.backArrow)

        // Control de volumen
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        seekBar.max = maxVolume
        seekBar.progress = currentVolume

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                prefs.edit().putInt("user_volume", progress).apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Mostrar u ocultar volumen
        volumeButton.setOnClickListener {
            volumeControl.visibility =
                if (volumeControl.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // Mostrar popup de idioma
        languageButton.setOnClickListener {
            showLanguagePopup()
        }

        // Volver
        backArrow.setOnClickListener {
            finish()
        }
    }

    private fun showLanguagePopup() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.popup_language, null)
        val leftArrow = dialogView.findViewById<ImageView>(R.id.left_arrow)
        val rightArrow = dialogView.findViewById<ImageView>(R.id.right_arrow)
        val languageText = dialogView.findViewById<TextView>(R.id.language_selected)

        // Obtener idioma actual desde SharedPreferences
        val currentLang = prefs.getString("language", "es") ?: "es"
        languageText.text = if (currentLang == "es") "ESPAÑOL" else "ENGLISH"

        // Declarar alertDialog antes de usarlo
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()

        // Función para alternar idioma
        fun toggleLanguage() {
            val newLang = if (languageText.text == "ESPAÑOL") "en" else "es"
            changeAppLanguage(newLang)
            alertDialog.dismiss()
        }

        // Asignar clics a ambas flechas
        leftArrow.setOnClickListener { toggleLanguage() }
        rightArrow.setOnClickListener { toggleLanguage() }

        alertDialog.show()
    }

    private fun changeAppLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Guardar idioma en preferencias
        prefs.edit().putString("language", languageCode).apply()

        // Reiniciar actividad para aplicar idioma
        recreate()
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
}
