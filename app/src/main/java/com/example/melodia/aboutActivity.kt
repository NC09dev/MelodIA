package com.example.melodia

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicar idioma antes de cargar la vista
        loadLocale()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_activity)

        val backArrow = findViewById<ImageView>(R.id.backArrow)

        // Flecha de regreso
        backArrow.setOnClickListener {
            finish()
        }

        // Habilitar bordes sin recortes
        enableEdgeToEdge()

        // Ocultar botones de navegación y barra de estado
        hideSystemUI()

        // Ajustar los insets del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.about_activity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val textView = findViewById<TextView>(R.id.option1)
        textView.setOnClickListener {
            val url = "https://github.com/NC09dev/MelodIA"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        val textView2 = findViewById<TextView>(R.id.option2)
        textView2.setOnClickListener {
            val url = "https://www.instagram.com/p/DH4uFL4O7S2/?img_index=1&igsh=djJoZnVyY3k2ZmQ4"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
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

    // Cargar el idioma guardado en SharedPreferences
    private fun loadLocale() {
        val sharedPrefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val language = sharedPrefs.getString("App_Lang", "es") // idioma por defecto: español
        setLocale(language)
    }

    // Cambiar el idioma de la app
    private fun setLocale(language: String?) {
        if (language == null) return
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            val prefs = getSharedPreferences("saved_songs", MODE_PRIVATE)
            prefs.edit().clear().apply()
        }
    }

}