package com.example.melodia

import android.content.Intent
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

    // Cargar idioma guardado en SharedPreferences "config"
    private fun loadLocale() {
        val sharedPreferences = getSharedPreferences("config", MODE_PRIVATE)
        val language = sharedPreferences.getString("language", "es") ?: "es"
        setLocale(language)
    }

    // Aplicar el idioma a la configuración
    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
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