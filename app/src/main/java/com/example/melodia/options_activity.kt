package com.example.melodia

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale
import android.content.res.Configuration

class Optionsactivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(updateLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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
            volumeControl.visibility =
                if (volumeControl.visibility == View.GONE) View.VISIBLE else View.GONE
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

        // Reiniciar bodyActivity para aplicar el idioma
        val intent = Intent(this, bodyActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun updateLocale(context: Context): Context {
        val prefs: SharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val language = prefs.getString("App_Lang", "es") ?: "es"

        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }

}