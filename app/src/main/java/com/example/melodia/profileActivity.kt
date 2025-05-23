package com.example.melodia

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class profileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicar idioma antes de inflar el layout
        loadLocale()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)

        val backArrow = findViewById<ImageView>(R.id.backArrow)

        // Habilitar bordes sin recortes
        enableEdgeToEdge()

        // Ocultar botones de navegación y barra de estado
        hideSystemUI()

        // Flecha de regreso
        backArrow.setOnClickListener {
            finish()
        }

        // Referenciar el TextView "CONTRASEÑA"
        auth = FirebaseAuth.getInstance()
        val archivoButton = findViewById<TextView>(R.id.archivoButton)
        val logoutTextView = findViewById<TextView>(R.id.exportarButton)

        // Asignar el listener para redirigir a PasswordActivity
        archivoButton.setOnClickListener {
            val intent = Intent(this@profileActivity, PasswordActivity::class.java)
            startActivity(intent)
        }

        logoutTextView.setOnClickListener {
            auth.signOut() // Cerrar sesión
            val intent = Intent(this@profileActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    // Cargar idioma desde SharedPreferences
    private fun loadLocale() {
        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        val language = prefs.getString("language", "es") ?: "es"
        setAppLanguage(language)
    }

    // Establecer idioma en la configuración
    private fun setAppLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    // Ocultar navegación y barra de estado
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
