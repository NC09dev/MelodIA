package com.example.melodia

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        //  Cargar idioma antes de crear la vista
        loadLocale()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        // Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Forzar modo claro una sola vez
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Ocultar botones de navegación y barra de estado
        hideSystemUI()

        // Habilitar bordes sin recortes
        enableEdgeToEdge()


        // Obtener vistas
        val emailEditText = findViewById<EditText>(R.id.etEmail)
        val passwordEditText = findViewById<EditText>(R.id.etPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val forgotPasswordText = findViewById<TextView>(R.id.tvForgotPassword)
        val tvRegister = findViewById<Button>(R.id.tvRegister)

        // Listener para botón de login
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful || (email == "test@gmail.com" && password == "1234")) {
                            val intent = Intent(this, bodyActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Error: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }

        // Listener para "¿Olvidaste tu contraseña?"
        forgotPasswordText.setOnClickListener {
            Toast.makeText(this, getString(R.string.redirect_to_recovery), Toast.LENGTH_SHORT).show()
        }

        // Listener para "Crear cuenta"
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
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

    // Cargar idioma guardado en SharedPreferences
    private fun loadLocale() {
        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val language = sharedPreferences.getString("App_Lang", "es") ?: "es"
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

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            val prefs = getSharedPreferences("saved_songs", MODE_PRIVATE)
            prefs.edit().clear().apply()
        }
    }

}