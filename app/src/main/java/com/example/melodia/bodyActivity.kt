package com.example.melodia

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import android.content.Context
import android.view.inputmethod.InputMethodManager

class bodyActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var handler: Handler
    private var isUserSeeking = false
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.body_activity)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        enableEdgeToEdge()
        hideSystemUI()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val aboutTextView = findViewById<TextView>(R.id.about)
        aboutTextView.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        val menuIcon = findViewById<ImageView>(R.id.settings)
        menuIcon.setOnClickListener {
            val popupView = layoutInflater.inflate(R.layout.popup_menu, null)
            val popupWindow = PopupWindow(popupView, 400, 400, true)

            val itemConfig = popupView.findViewById<TextView>(R.id.configuration_menu)
            val itemMelodias = popupView.findViewById<TextView>(R.id.saves_menu)
            val itemPerfil = popupView.findViewById<TextView>(R.id.profile_menu)

            itemConfig.setOnClickListener {
                startActivity(Intent(this, Optionsactivity::class.java))
                popupWindow.dismiss()
            }

            itemMelodias.setOnClickListener {
                startActivity(Intent(this, Savesactivity::class.java))
                popupWindow.dismiss()
            }

            itemPerfil.setOnClickListener {
                startActivity(Intent(this, Profileactivity::class.java))
                popupWindow.dismiss()
            }

            popupWindow.elevation = 10f
            popupWindow.showAsDropDown(menuIcon, 0, 20)
        }

        // 🎵 Reproductor
        mediaPlayer = MediaPlayer.create(this, R.raw.test)
        seekBar = findViewById(R.id.seekBar)
        val playPauseBtn = findViewById<ImageButton>(R.id.btnPlayPause)

        seekBar.max = mediaPlayer.duration
        handler = Handler(Looper.getMainLooper())

        val updateSeekBar = object : Runnable {
            override fun run() {
                if (!isUserSeeking) {
                    seekBar.progress = mediaPlayer.currentPosition
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateSeekBar)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mediaPlayer.seekTo(progress)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) { isUserSeeking = true }
            override fun onStopTrackingTouch(sb: SeekBar?) { isUserSeeking = false }
        })

        playPauseBtn.setOnClickListener {
            if (isPlaying) {
                mediaPlayer.pause()
                playPauseBtn.setImageResource(R.drawable.play)
            } else {
                mediaPlayer.start()
                playPauseBtn.setImageResource(R.drawable.pause)
            }
            isPlaying = !isPlaying
        }

        mediaPlayer.setOnCompletionListener {
            seekBar.progress = 0
            playPauseBtn.setImageResource(R.drawable.play)
            isPlaying = false
        }

        // 🎤 Generar música con prompt
        val promptBtn = findViewById<ImageView>(R.id.prompt)
        val chatBox = findViewById<EditText>(R.id.chatbox)

        promptBtn.setOnClickListener {
            val texto = chatBox.text.toString().trim()

            when {
                texto.isEmpty() -> {
                    Toast.makeText(this, "Escribe un mensaje para generar música 🎵", Toast.LENGTH_SHORT).show()
                }
                texto.length > 100 -> {
                    Toast.makeText(this, "El texto es demasiado largo. Máximo 200 caracteres.", Toast.LENGTH_LONG).show()
                }
                else -> {
                    generarMusicaDesdePrompt(texto)
                    // Deseleccionar el cuadro de texto (quitar el foco)
                    chatBox.clearFocus()

                    // Cerrar el teclado
                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    inputMethodManager?.hideSoftInputFromWindow(chatBox.windowToken, 0)

                }
            }
        }



    }

    private fun generarMusicaDesdePrompt(prompt: String) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("prompt", JSONObject().put("text", prompt))
            put("format", "wav")  // Opcional: puedes cambiar esto a "mp3" o "aac"
            put("looping", false)  // Opcional: pon esto en true para más bucles
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url("https://public-api.beatoven.ai/api/v1/tracks/compose")  // URL correcta
            .addHeader("Authorization", "Bearer LnbcvkbQPBHwSYotbkkE9g")
            .addHeader("Content-Type", "application/json")  // Cabecera de tipo de contenido
            .post(requestBody)
            .build()

        // Mostrar Toast mientras se genera la música
        runOnUiThread {
            Toast.makeText(this, "Componiendo música, por favor espera...", Toast.LENGTH_SHORT).show()
        }

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@bodyActivity, "Error al conectar con Beatoven: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                println("Response Body: $responseBody") // Registro de depuración

                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@bodyActivity, "Error en la API: ${response.code} - ${response.message}", Toast.LENGTH_LONG).show()
                        Toast.makeText(this@bodyActivity, "Cuerpo de la respuesta: $responseBody", Toast.LENGTH_LONG).show()
                    }
                    return
                }

                try {
                    val jsonResponse = JSONObject(responseBody)
                    val taskId = jsonResponse.getString("task_id")  // Obtener el task_id de la respuesta
                    runOnUiThread {
                        Toast.makeText(this@bodyActivity, "Tarea iniciada, task_id: $taskId", Toast.LENGTH_SHORT).show()
                    }
                    consultarEstadoDeTarea(taskId)  // Consultar el estado de la tarea
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@bodyActivity, "Error al procesar la respuesta: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun consultarEstadoDeTarea(taskId: String) {
        val client = OkHttpClient()
        val url = "https://public-api.beatoven.ai/api/v1/tasks/$taskId"  // URL correcta

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer LnbcvkbQPBHwSYotbkkE9g")
            .get()
            .build()

        Handler(Looper.getMainLooper()).postDelayed({
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@bodyActivity, "Error al consultar el estado de la canción: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val bodyStr = response.body?.string()
                    println("Response Body: $bodyStr")  // Ver respuesta en Logcat

                    if (!response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@bodyActivity, "Error al consultar estado: ${response.code} - ${response.message}", Toast.LENGTH_LONG).show()
                            Toast.makeText(this@bodyActivity, "Cuerpo de la respuesta: $bodyStr", Toast.LENGTH_LONG).show()
                        }
                        return
                    }

                    try {
                        val body = JSONObject(bodyStr ?: "{}")
                        val status = body.optString("status", "pending")

                        runOnUiThread {
                            Toast.makeText(this@bodyActivity, "Estado de la tarea: $status", Toast.LENGTH_SHORT).show()
                        }

                        if (status == "composed") {
                            val trackUrl = body.optString("track_url", null)
                            if (trackUrl != null && trackUrl.isNotEmpty()) {
                                runOnUiThread {
                                    Toast.makeText(this@bodyActivity, "¡Música lista! 🎶", Toast.LENGTH_SHORT).show()
                                    reproducirDesdeUrl(trackUrl)  // Reproducir la pista cuando esté lista
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@bodyActivity, "No se encontró la URL del audio. Intenta nuevamente.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // Si el estado aún no está "composed", intenta nuevamente después de un tiempo
                            consultarEstadoDeTarea(taskId)  // Reintentar
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@bodyActivity, "Error al procesar la respuesta: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            })
        }, 5000)  // Espera de 5 segundos
    }





    private fun reproducirDesdeUrl(audioUrl: String) {
        mediaPlayer.release()  // Release the previous media player instance if it exists
        mediaPlayer = MediaPlayer()  // Create a new instance
        mediaPlayer.setDataSource(audioUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            it.start()
            findViewById<ImageButton>(R.id.btnPlayPause).setImageResource(R.drawable.pause)
            isPlaying = true
        }
    }




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

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        mediaPlayer.release()
    }
}
