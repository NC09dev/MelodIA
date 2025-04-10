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

        // Menú desplegable personalizado
        val menuIcon = findViewById<ImageView>(R.id.settings)
        menuIcon.setOnClickListener {
            val inflater = layoutInflater
            val popupView = inflater.inflate(R.layout.popup_menu, null)

            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )

            popupWindow.elevation = 10f
            popupWindow.setBackgroundDrawable(getDrawable(R.drawable.popup_background))
            popupWindow.isOutsideTouchable = true

            // Mostrar debajo del ícono
            popupWindow.showAsDropDown(menuIcon, 0, 10)

            // Listeners de opciones del menú
            val configuracion = popupView.findViewById<TextView>(R.id.menu_configuracion)
            val melodias = popupView.findViewById<TextView>(R.id.menu_mis_melodias)
            val perfil = popupView.findViewById<TextView>(R.id.menu_perfil)

            configuracion.setOnClickListener {
                // Acción para configuración
                popupWindow.dismiss()
            }

            melodias.setOnClickListener {
                // Acción para melodías
                popupWindow.dismiss()
            }

            perfil.setOnClickListener {
                // Acción para perfil
                popupWindow.dismiss()
            }
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
