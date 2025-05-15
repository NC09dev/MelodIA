package com.example.melodia

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgotpassword_activity)

        auth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.etForgotEmail)
        sendButton = findViewById(R.id.btnSendReset)
        backButton = findViewById(R.id.btnBackToLogin)

        sendButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa tu correo", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(this, "📧 Se envió un correo para restablecer tu contraseña", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        backButton.setOnClickListener {
            finish() // Regresa al LoginActivity
        }
    }
}
