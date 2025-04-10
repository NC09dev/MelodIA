package com.example.melodia

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageView
import android.view.LayoutInflater
import android.widget.PopupWindow

class bodyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Establecer el layout
        setContentView(R.layout.body_activity)

        // Forzar modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Habilitar bordes sin recortes
        enableEdgeToEdge()

        // Ocultar botones de navegación y barra de estado
        hideSystemUI()

        // Ajustar los insets del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ir a AboutActivity desde el TextView
        val aboutTextView = findViewById<TextView>(R.id.about)
        aboutTextView.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        val menuIcon = findViewById<ImageView>(R.id.settings)
        menuIcon.setOnClickListener {
            val popupView = layoutInflater.inflate(R.layout.popup_menu, null)
            val popupWindow = PopupWindow(popupView, 400, 400, true)

            val itemConfig = popupView.findViewById<TextView>(R.id.configuration_menu)
            val itemMelodias = popupView.findViewById<TextView>(R.id.saves_menu)
            val itemPerfil = popupView.findViewById<TextView>(R.id.profile_menu)

            itemConfig.setOnClickListener {
                val intent = Intent(this, Optionsactivity::class.java)
                startActivity(intent)
                popupWindow.dismiss()
            }

            itemMelodias.setOnClickListener {
                val intent = Intent(this, Savesactivity::class.java)
                startActivity(intent)
                popupWindow.dismiss()
            }

            itemPerfil.setOnClickListener {
                val intent = Intent(this, Profileactivity::class.java)
                startActivity(intent)
                popupWindow.dismiss()
            }

            popupWindow.elevation = 10f
            popupWindow.showAsDropDown(menuIcon, 0, 20)
        }

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
