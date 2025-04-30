package com.example.melodia

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import kotlin.random.Random

class bodyActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var handler: Handler
    private var isUserSeeking = false
    private var isPlaying = false
    private var currentTrackUrl: String? = null
    private lateinit var mainImage: ImageView
    private val imageList = listOf(
        R.drawable.design1,
        R.drawable.design2,
        R.drawable.design3,
        R.drawable.design4,
        R.drawable.design5,
        R.drawable.design6,
        R.drawable.design7,
        R.drawable.design8
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.body_activity)

        // Habilitar bordes sin recortes
        enableEdgeToEdge()

        // Ocultar botones de navegación y barra de estado
        hideSystemUI()

        handler = Handler(Looper.getMainLooper())

        seekBar = findViewById(R.id.seekBar)
        mainImage = findViewById(R.id.imageView)
        mainImage.setImageResource(R.drawable.icono) // imagen inicial

        val playPauseBtn = findViewById<ImageButton>(R.id.btnPlayPause)

        mediaPlayer = MediaPlayer.create(this, R.raw.test)
        seekBar.max = mediaPlayer.duration

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
            seekBar.progress = seekBar.max
            playPauseBtn.setImageResource(R.drawable.play)
            isPlaying = false
            handler.postDelayed({ seekBar.progress = 0 }, 500)
        }

        val promptBtn = findViewById<ImageView>(R.id.prompt)
        val chatBox = findViewById<EditText>(R.id.chatbox)

        promptBtn.setOnClickListener {
            val texto = chatBox.text.toString().trim()
            if (texto.isEmpty()) {
                Toast.makeText(this, "Escribe un mensaje para generar música 🎵", Toast.LENGTH_SHORT).show()
            } else {
                generarMusicaDesdePrompt(texto)
                chatBox.clearFocus()
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                inputMethodManager?.hideSoftInputFromWindow(chatBox.windowToken, 0)
            }
        }

        val random_btn = findViewById<TextView>(R.id.random)

        random_btn.setOnClickListener{
            generarMusicaDesdePrompt("random")
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
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isPlaying", isPlaying)
        outState.putInt("seekProgress", seekBar.progress)
        currentTrackUrl?.let { outState.putString("trackUrl", it) }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isPlaying = savedInstanceState.getBoolean("isPlaying", false)
        val progress = savedInstanceState.getInt("seekProgress", 0)
        val url = savedInstanceState.getString("trackUrl", null)

        if (url != null) {
            reproducirDesdeUrl(url, resumeAt = progress, autoStart = isPlaying)
        } else {
            seekBar.progress = progress
        }
    }

    private fun reproducirDesdeUrl(audioUrl: String, resumeAt: Int = 0, autoStart: Boolean = true) {
        currentTrackUrl = audioUrl
        mediaPlayer.release()
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(audioUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            seekBar.max = mediaPlayer.duration
            if (resumeAt > 0) mediaPlayer.seekTo(resumeAt)
            if (autoStart) {
                mediaPlayer.start()
                val nuevaImagen = imageList[Random.nextInt(imageList.size)]
                mainImage.setImageResource(nuevaImagen)
                findViewById<ImageButton>(R.id.btnPlayPause).setImageResource(R.drawable.pause)
                isPlaying = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        mediaPlayer.release()
    }

    private fun generarMusicaDesdePrompt(prompt: String) {
        runOnUiThread {
            Toast.makeText(this@bodyActivity, "🔄 Componiendo música, por favor espera...", Toast.LENGTH_SHORT).show()
        }

        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("prompt", JSONObject().put("text", prompt))
            put("format", "wav")
            put("looping", false)
            put("duration", 30)
            put("genre", "ambient")
            put("mood", "calm")
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url("https://public-api.beatoven.ai/api/v1/tracks/compose")
            .addHeader("Authorization", "Bearer LnbcvkbQPBHwSYotbkkE9g")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@bodyActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string()
                if (!response.isSuccessful || bodyStr.isNullOrEmpty()) {
                    runOnUiThread {
                        Toast.makeText(this@bodyActivity, "Error al componer música", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val jsonResponse = JSONObject(bodyStr)
                val taskId = jsonResponse.optString("task_id")
                if (taskId.isNotEmpty()) {
                    runOnUiThread {
                    }
                    consultarEstadoDeTarea(taskId)
                }
            }
        })
    }

    private fun consultarEstadoDeTarea(taskId: String, reintentos: Int = 0) {
        val client = OkHttpClient()
        val url = "https://public-api.beatoven.ai/api/v1/tasks/$taskId"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer LnbcvkbQPBHwSYotbkkE9g")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@bodyActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string()
                val body = JSONObject(bodyStr ?: "{}")
                val status = body.optString("status", "unknown")
                val meta = body.optJSONObject("meta")
                val trackUrl = meta?.optString("track_url") ?: meta?.optString("audio_url")

                when (status) {
                    "composed" -> {
                        if (!trackUrl.isNullOrEmpty()) {
                            runOnUiThread {
                                Toast.makeText(this@bodyActivity, "¡Música lista!", Toast.LENGTH_SHORT).show()
                                reproducirDesdeUrl(trackUrl)
                            }
                        } else if (reintentos < 3) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                consultarEstadoDeTarea(taskId, reintentos + 1)
                            }, 3000)
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@bodyActivity, "No se pudo obtener la pista.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    "pending", "in_progress", "composing" -> {
                        Handler(Looper.getMainLooper()).postDelayed({
                            consultarEstadoDeTarea(taskId, reintentos)
                        }, 5000)
                    }
                    else -> {
                        runOnUiThread {
                            Toast.makeText(this@bodyActivity, "Estado inesperado: $status", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
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
}
