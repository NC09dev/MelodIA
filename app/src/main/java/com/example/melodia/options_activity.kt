package com.example.melodia

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Optionsactivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilitar bordes sin recortes
        enableEdgeToEdge()

        setContentView(R.layout.options_activity)

        // Ocultar botones de navegación y barra de estado
        hideSystemUI()

        // Referencias a los elementos del layout
        val volumeButton = findViewById<TextView>(R.id.volumeButton)
        val volumeControl = findViewById<LinearLayout>(R.id.volumeControl)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)

        // Mostrar u ocultar el control de volumen al tocar el botón
        volumeButton.setOnClickListener {
            if (volumeControl.visibility == View.GONE) {
                volumeControl.visibility = View.VISIBLE
            } else {
                volumeControl.visibility = View.GONE
            }
        }

        // Lógica del SeekBar (por ahora solo imprime el valor en consola)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Aquí puedes ajustar el volumen o guardar el valor
                println("Volumen actual: $progress")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Funcionalidad de la flecha de regreso
        val backArrow = findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener {
            finish() // Cierra la actividad actual y vuelve a la anterior
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
