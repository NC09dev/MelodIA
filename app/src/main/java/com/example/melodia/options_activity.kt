package com.example.melodia

import android.content.Intent
import android.content.SharedPreferences
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

        // Habilitar bordes sin recortes
        enableEdgeToEdge()

        setContentView(R.layout.options_activity)

        // Ocultar botones de navegación y barra de estado
        hideSystemUI()

        // Referencias a los elementos del layout
        val volumeButton = findViewById<TextView>(R.id.volumeButton)
        val volumeControl = findViewById<LinearLayout>(R.id.volumeControl)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val languageButton = findViewById<TextView>(R.id.languajeButton)
        val backArrow = findViewById<ImageView>(R.id.backArrow)

        // Mostrar u ocultar el control de volumen al tocar el botón
        volumeButton.setOnClickListener {
            volumeControl.visibility = if (volumeControl.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // Cambiar idioma al hacer clic en el botón
        languageButton.setOnClickListener {
            toggleLanguage()
        }

        // Lógica del SeekBar (por ahora solo imprime el valor en consola)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                println("Volumen actual: $progress")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Flecha de regreso
        backArrow.setOnClickListener {
            finish()
        }
    }

    // Ocultar sistema UI
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

    // Cambiar idioma y guardar en SharedPreferences
    private fun toggleLanguage() {
        val currentLocale = resources.configuration.locales[0].language
        val newLocale = if (currentLocale == "es") "en" else "es"

        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("App_Lang", newLocale)
            apply()
        }

        setLocale(newLocale)

        // Reiniciar la actividad para aplicar el idioma
        val intent = Intent(this, Optionsactivity::class.java)
        finish()
        startActivity(intent)
    }

    // Aplicar el idioma desde SharedPreferences
    private fun loadLocale() {
        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val language = sharedPreferences.getString("App_Lang", "es") ?: "es"
        setLocale(language)
    }

    // Establecer el idioma en la configuración
    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}