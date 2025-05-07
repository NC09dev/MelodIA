package com.example.melodia

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.util.*

class bodyActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var handler: Handler
    private var isUserSeeking = false
    private var isPlaying = false
    private var currentTrackUrl: String? = null
    private lateinit var mainImage: ImageView
    private lateinit var spinner: ProgressBar
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
        aplicarIdiomaGuardado()
        setContentView(R.layout.body_activity)
        enableEdgeToEdge()
        hideSystemUI()

        handler = Handler(Looper.getMainLooper())

        seekBar = findViewById(R.id.seekBar)
        seekBar.max = 100
        mainImage = findViewById(R.id.imageView)
        mainImage.setImageResource(R.drawable.icono)

        spinner = findViewById(R.id.progressBar)
        spinner.indeterminateTintList = ColorStateList.valueOf(Color.parseColor("#7BEEAF"))
        spinner.visibility = View.GONE

        val playPauseBtn = findViewById<ImageButton>(R.id.btnPlayPause)
        mediaPlayer = MediaPlayer()

        // Recupera y aplica el volumen guardado
        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        val savedVolume = prefs.getInt("user_volume", -1)

        if (savedVolume != -1) {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, savedVolume, 0)
        }

        val updateSeekBar = object : Runnable {
            override fun run() {
                if (!isUserSeeking && mediaPlayer.isPlaying) {
                    seekBar.progress = mediaPlayer.currentPosition
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateSeekBar)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && mediaPlayer.isPlaying) mediaPlayer.seekTo(progress)
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
                showToast(R.string.empty_prompt)
            } else {
                generarMusicaDesdePrompt(texto)
                chatBox.clearFocus()
                (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                    ?.hideSoftInputFromWindow(chatBox.windowToken, 0)
            }
        }

        findViewById<TextView>(R.id.random).setOnClickListener {
            seekBar.visibility = View.VISIBLE
            playPauseBtn.visibility = View.GONE
            generarMusicaDesdePrompt("random")
        }

        findViewById<TextView>(R.id.about).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        findViewById<ImageView>(R.id.settings).setOnClickListener {
            val popupView = layoutInflater.inflate(R.layout.popup_menu, null)
            val popupWindow = PopupWindow(popupView, 400, 400, true)

            popupView.findViewById<TextView>(R.id.configuration_menu).setOnClickListener {
                startActivity(Intent(this, Optionsactivity::class.java))
                popupWindow.dismiss()
            }

            popupView.findViewById<TextView>(R.id.saves_menu).setOnClickListener {
                startActivity(Intent(this, Savesactivity::class.java))
                popupWindow.dismiss()
            }

            popupView.findViewById<TextView>(R.id.profile_menu).setOnClickListener {
                startActivity(Intent(this, profileActivity::class.java))
                popupWindow.dismiss()
            }

            popupWindow.elevation = 10f
            popupWindow.showAsDropDown(findViewById(R.id.settings), 0, 20)
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

        if (::mediaPlayer.isInitialized) {
            try {
                mediaPlayer.reset()
            } catch (e: Exception) {
                mediaPlayer.release()
                mediaPlayer = MediaPlayer()
            }
        } else {
            mediaPlayer = MediaPlayer()
        }

        mediaPlayer.setDataSource(audioUrl)
        mediaPlayer.setOnPreparedListener {
            seekBar.max = mediaPlayer.duration
            if (resumeAt > 0) mediaPlayer.seekTo(resumeAt)
            if (autoStart) {
                mediaPlayer.start()
                mainImage.setImageResource(imageList.random())
                findViewById<ImageButton>(R.id.btnPlayPause).setImageResource(R.drawable.pause)
                isPlaying = true
            }
        }
        mediaPlayer.setOnCompletionListener {
            seekBar.progress = seekBar.max
            findViewById<ImageButton>(R.id.btnPlayPause).setImageResource(R.drawable.play)
            isPlaying = false
            handler.postDelayed({ seekBar.progress = 0 }, 500)
        }
        mediaPlayer.prepareAsync()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        if (::mediaPlayer.isInitialized) mediaPlayer.release()
    }

    private fun generarMusicaDesdePrompt(prompt: String) {
        runOnUiThread {
            findViewById<ImageButton>(R.id.btnPlayPause).visibility = View.GONE
            spinner.visibility = View.VISIBLE
            showToast(R.string.composing_music)
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
                showToast(R.string.network_error)
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string()
                if (!response.isSuccessful || bodyStr.isNullOrEmpty()) {
                    showToast(R.string.music_error)
                    return
                }

                val taskId = JSONObject(bodyStr).optString("task_id")
                if (taskId.isNotEmpty()) {
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
                showToast(R.string.network_error)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = JSONObject(response.body?.string() ?: "{}")
                val status = body.optString("status", "unknown")
                val meta = body.optJSONObject("meta")
                val trackUrl = meta?.optString("track_url") ?: meta?.optString("audio_url")

                when (status) {
                    "composed" -> {
                        if (!trackUrl.isNullOrEmpty()) {
                            runOnUiThread {
                                showToast(R.string.music_ready)
                                findViewById<ImageButton>(R.id.btnPlayPause).visibility = View.VISIBLE
                                spinner.visibility = View.GONE
                                reproducirDesdeUrl(trackUrl)
                            }
                        } else if (reintentos < 3) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                consultarEstadoDeTarea(taskId, reintentos + 1)
                            }, 3000)
                        } else {
                            showToast(R.string.track_not_found)
                        }
                    }
                    "pending", "in_progress", "composing" -> {
                        Handler(Looper.getMainLooper()).postDelayed({
                            consultarEstadoDeTarea(taskId, reintentos)
                        }, 5000)
                    }
                    else -> showToast(R.string.unexpected_status)
                }
            }
        })
    }

    private fun showToast(messageResId: Int) {
        runOnUiThread {
            Toast.makeText(this, getString(messageResId), Toast.LENGTH_SHORT).show()
        }
    }

    private fun aplicarIdiomaGuardado() {
        val prefs: SharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE)
        val idioma = prefs.getString("language", Locale.getDefault().language)
        val locale = Locale(idioma ?: "en")
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
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
