package com.example.melodia

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class PasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        //  Cargar el idioma antes de crear la vista
        loadLocale()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.password_activity)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        val newPasswordField = findViewById<EditText>(R.id.password3)
        val confirmPasswordField = findViewById<EditText>(R.id.etConfirm)
        val confirmButton = findViewById<Button>(R.id.btnLogin)

        confirmButton.setOnClickListener {
            val newPassword = newPasswordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_both_fields), Toast.LENGTH_SHORT).show()
            } else if (newPassword != confirmPassword) {
                Toast.makeText(this, getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show()
            } else {
                val user = auth.currentUser
                if (user != null) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, getString(R.string.password_updated), Toast.LENGTH_SHORT).show()
                                finish() // Vuelve a la pantalla anterior
                            } else {
                                Toast.makeText(this, getString(R.string.error_update_password) + ": ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(this, getString(R.string.user_not_authenticated), Toast.LENGTH_SHORT).show()
                }
            }
        }
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
}