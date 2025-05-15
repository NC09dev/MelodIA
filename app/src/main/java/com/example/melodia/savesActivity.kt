package com.example.melodia

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class Savesactivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cargar idioma desde SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val language = sharedPreferences.getString("language", "es")
        setAppLanguage(language ?: "es")

        enableEdgeToEdge()
        setContentView(R.layout.saves_activity)
        hideSystemUI()

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener { finish() }

        findViewById<TextView>(R.id.archivoButton).setOnClickListener {
            startActivity(Intent(this, Listactivity::class.java))
        }

        findViewById<TextView>(R.id.exportarButton).setOnClickListener {
            startActivity(Intent(this, exportActivity::class.java))
        }


    }

    // Cambiar el idioma de la aplicación
    private fun setAppLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Método para ocultar los botones de navegación y la barra de estado
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