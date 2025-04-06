package com.example.melodia

import android.content.Intent
import android.view.View
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        // Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Habilitar bordes sin recortes
        enableEdgeToEdge()

        // Obtener vistas
        val emailEditText = findViewById<EditText>(R.id.etEmail)
        val passwordEditText = findViewById<EditText>(R.id.etPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val forgotPasswordText = findViewById<TextView>(R.id.tvForgotPassword)
        val createAccountText = findViewById<TextView>(R.id.tvRegister)

        // Listener para botón de login
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                // Aquí puedes validar contra una base de datos, o simular con valores fijos
                if (email == "test@gmail.com" && password == "1234") {
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

                    // Ir a otra actividad (MainActivity por ejemplo)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Listener para "¿Olvidaste tu contraseña?"
        forgotPasswordText.setOnClickListener {
            Toast.makeText(this, "Redirigir a recuperación de contraseña", Toast.LENGTH_SHORT).show()
        }

        // Listener para "Crear cuenta"
        createAccountText.setOnClickListener {
            Toast.makeText(this, "Redirigir a registro", Toast.LENGTH_SHORT).show()
        }
    }
}

