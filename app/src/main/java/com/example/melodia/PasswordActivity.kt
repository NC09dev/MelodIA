package com.example.melodia

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class PasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.password_activity) // Asegúrate de que este nombre coincida

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        val newPasswordField = findViewById<EditText>(R.id.password3)
        val confirmPasswordField = findViewById<EditText>(R.id.etConfirm)
        val confirmButton = findViewById<Button>(R.id.btnLogin)

        confirmButton.setOnClickListener {
            val newPassword = newPasswordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Rellena ambos campos", Toast.LENGTH_SHORT).show()
            } else if (newPassword != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            } else {
                val user = auth.currentUser
                if (user != null) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                                finish() // Vuelve a la pantalla anterior
                            } else {
                                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
