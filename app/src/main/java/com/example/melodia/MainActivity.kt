package com.example.melodia

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var volumeButton: TextView
    private lateinit var volumeControl: LinearLayout
    private lateinit var volumeSeekBar: SeekBar
    private var isVolumeControlVisible = false
    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Primero establecemos el layout
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

        // Configurar evento de clic en el TextView para ir a AboutActivity
        val aboutTextView = findViewById<TextView>(R.id.about)
        aboutTextView.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        // Inicializar controles de volumen
        initVolumeControls()
    }

    private fun initVolumeControls() {
        volumeButton = findViewById(R.id.volumeButton)
        volumeControl = findViewById(R.id.volumeControl)
        volumeSeekBar = findViewById(R.id.volumeSeekBar)

        // Configurar el AudioManager para controlar el volumen del sistema
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        // Configurar el rango máximo del SeekBar según el volumen máximo del sistema
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        volumeSeekBar.max = maxVolume

        // Establecer el progreso actual del SeekBar según el volumen actual
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        volumeSeekBar.progress = currentVolume

        // Configurar el click listener para mostrar/ocultar el control de volumen
        volumeButton.setOnClickListener {
            toggleVolumeControl()
        }

        // Configurar el SeekBar para controlar el volumen real del dispositivo
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        progress,
                        AudioManager.FLAG_SHOW_UI
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun toggleVolumeControl() {
        if (isVolumeControlVisible) {
            volumeControl.visibility = View.GONE
        } else {
            volumeControl.visibility = View.VISIBLE
            // Actualizar el SeekBar con el volumen actual cada vez que se muestra
            volumeSeekBar.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        }
        isVolumeControlVisible = !isVolumeControlVisible
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