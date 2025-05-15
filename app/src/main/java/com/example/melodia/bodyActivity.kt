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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.util.*
import android.animation.ValueAnimator
import androidx.constraintlayout.widget.ConstraintLayout
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


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
        loadLocale()
        setContentView(R.layout.body_activity)
        enableEdgeToEdge()
        hideSystemUI()

        handler = Handler(Looper.getMainLooper())

        val layout = findViewById<ConstraintLayout>(R.id.main)
        val originalColor = Color.parseColor("#f8b23b")
        val playingColor = Color.parseColor("#ff9d6d")

        seekBar = findViewById(R.id.seekBar)
        seekBar.max = 100
        mainImage = findViewById(R.id.imageView)
        mainImage.setImageResource(R.drawable.icono)

        spinner = findViewById(R.id.progressBar)
        spinner.indeterminateTintList = ColorStateList.valueOf(Color.parseColor("#7BEEAF"))
        spinner.visibility = View.GONE

        val playPauseBtn = findViewById<ImageButton>(R.id.btnPlayPause)
        playPauseBtn.visibility = View.GONE

        mediaPlayer = MediaPlayer()

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
                handler.postDelayed(this, 200)
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
                animateBackgroundColor(layout, playingColor, originalColor)
            } else {
                mediaPlayer.start()
                playPauseBtn.setImageResource(R.drawable.pause)
                animateBackgroundColor(layout, originalColor, playingColor)
            }
            isPlaying = !isPlaying
        }

        mediaPlayer.setOnCompletionListener {
            seekBar.progress = seekBar.max
            findViewById<ImageButton>(R.id.btnPlayPause).setImageResource(R.drawable.play)
            isPlaying = false

            handler.postDelayed({ seekBar.progress = 0 }, 500)
            animateBackgroundColor(layout, playingColor, originalColor)
        }

        val heartBtn = findViewById<ImageView>(R.id.heart)
        heartBtn.setOnClickListener {
            currentTrackUrl?.let { url ->
                val input = EditText(this)
                input.hint = "Nombre de la canción"

                AlertDialog.Builder(this)
                    .setTitle("Guardar canción")
                    .setView(input)
                    .setPositiveButton("Guardar") { _, _ ->
                        val name = input.text.toString().trim()
                        if (name.isNotEmpty()) {
                            val db = FirebaseFirestore.getInstance()
                            val userId = FirebaseAuth.getInstance().currentUser?.uid

                            if (userId != null) {
                                val songData = hashMapOf(
                                    "title" to name,
                                    "url" to url,
                                    "timestamp" to System.currentTimeMillis()
                                )
                                db.collection("users")
                                    .document(userId)
                                    .collection("songs")
                                    .add(songData)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "🎵 Canción guardada en la nube", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "❗ Error al guardar en Firebase", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(this, "❗ Usuario no autenticado", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "❗ El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } ?: run {
                Toast.makeText(this, "⚠️ No hay canción para guardar", Toast.LENGTH_SHORT).show()
            }
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
                if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    mediaPlayer.seekTo(0)
                    val playPauseBtn = findViewById<ImageButton>(R.id.btnPlayPause)
                    playPauseBtn.setImageResource(R.drawable.play)
                    isPlaying = false
                    animateBackgroundColor(layout, playingColor, originalColor)

                }
                popupWindow.dismiss()
            }

            popupView.findViewById<TextView>(R.id.saves_menu).setOnClickListener {
                startActivity(Intent(this, Savesactivity::class.java))

                if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    mediaPlayer.seekTo(0)
                    val playPauseBtn = findViewById<ImageButton>(R.id.btnPlayPause)
                    playPauseBtn.setImageResource(R.drawable.play)
                    isPlaying = false
                    animateBackgroundColor(layout, playingColor, originalColor)
                }

                popupWindow.dismiss()
            }


            popupView.findViewById<TextView>(R.id.profile_menu).setOnClickListener {
                startActivity(Intent(this, profileActivity::class.java))
                if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    mediaPlayer.seekTo(0)
                    val playPauseBtn = findViewById<ImageButton>(R.id.btnPlayPause)
                    playPauseBtn.setImageResource(R.drawable.play)
                    isPlaying = false
                    animateBackgroundColor(layout, playingColor, originalColor)
                }
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

        // Asegurar reinicio correcto del MediaPlayer
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
                isPlaying = true

                runOnUiThread {
                    val layout = findViewById<ConstraintLayout>(R.id.main)
                    val originalColor = Color.parseColor("#f8b23b")
                    val playingColor = Color.parseColor("#ff9d6d")

                    val playPauseBtn = findViewById<ImageButton>(R.id.btnPlayPause)
                    playPauseBtn.visibility = View.VISIBLE
                    playPauseBtn.setImageResource(R.drawable.pause)

                    spinner.visibility = View.GONE
                    mainImage.setImageResource(imageList.random())
                    animateBackgroundColor(layout, originalColor, playingColor)
                }
            }
        }


        mediaPlayer.setOnCompletionListener {
            seekBar.progress = seekBar.max
            isPlaying = false

            runOnUiThread {
                val layout = findViewById<ConstraintLayout>(R.id.main)
                val originalColor = Color.parseColor("#f8b23b")
                val playingColor = Color.parseColor("#ff9d6d")

                findViewById<ImageButton>(R.id.btnPlayPause).setImageResource(R.drawable.play)
                animateBackgroundColor(layout, playingColor, originalColor)
            }

            handler.postDelayed({ seekBar.progress = 0 }, 500)
        }

        mediaPlayer.prepareAsync()
    }




    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        if (::mediaPlayer.isInitialized) mediaPlayer.release()
        isPlaying = false

        // Borrar las canciones guardadas
        val prefs = getSharedPreferences("saved_songs", MODE_PRIVATE)
        prefs.edit().clear().apply()
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
            .addHeader("Authorization", "Bearer x1SZHDPKx9LNxL5dNauXyQ")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    spinner.visibility = View.GONE
                    showToast(R.string.network_error)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string()

                if (!response.isSuccessful || bodyStr.isNullOrEmpty()) {
                    runOnUiThread {
                        spinner.visibility = View.GONE
                        showToast(R.string.music_error)
                    }
                    return
                }

                val taskId = JSONObject(bodyStr).optString("task_id")
                if (taskId.isNotEmpty()) {
                    consultarEstadoDeTarea(taskId)
                } else {
                    runOnUiThread {
                        spinner.visibility = View.GONE
                        showToast(R.string.track_not_found)
                    }
                }
            }
        })
    }


    private var isCheckingTask = false
    private var hasShownWaitToast = false

    private fun consultarEstadoDeTarea(taskId: String, reintentos: Int = 0) {
        // Mostrar mensaje si ha pasado mucho tiempo y aún está generando
        if (reintentos == 3 && !hasShownWaitToast) {
            hasShownWaitToast = true
            runOnUiThread {
                Toast.makeText(this, "🎶 Esto está tardando un poco... ¡Gracias por esperar!", Toast.LENGTH_LONG).show()
            }
        }

        val client = OkHttpClient()
        val url = "https://public-api.beatoven.ai/api/v1/tasks/$taskId"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer x1SZHDPKx9LNxL5dNauXyQ")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    spinner.visibility = View.GONE
                    showToast(R.string.network_error)
                }
                isCheckingTask = false
                hasShownWaitToast = false
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string()

                if (bodyStr.isNullOrEmpty()) {
                    runOnUiThread {
                        spinner.visibility = View.GONE
                        showToast(R.string.track_not_found)
                    }
                    isCheckingTask = false
                    hasShownWaitToast = false
                    return
                }

                val body = JSONObject(bodyStr)
                val status = body.optString("status", "unknown")
                val meta = body.optJSONObject("meta")
                val trackUrl = meta?.optString("track_url") ?: meta?.optString("audio_url")

                when (status) {
                    "composed" -> {
                        isCheckingTask = false
                        hasShownWaitToast = false
                        if (!trackUrl.isNullOrEmpty()) {
                            runOnUiThread {
                                reproducirDesdeUrl(trackUrl)
                                showToast(R.string.music_ready)
                            }
                        } else {
                            runOnUiThread {
                                spinner.visibility = View.GONE
                                showToast(R.string.track_not_found)
                            }
                        }
                    }

                    "pending", "in_progress", "composing" -> {
                        if (reintentos < 10) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                consultarEstadoDeTarea(taskId, reintentos + 1)
                            }, 5000)
                        } else {
                            runOnUiThread {
                                spinner.visibility = View.GONE
                                showToast(R.string.track_not_found)
                            }
                            hasShownWaitToast = false
                        }
                    }

                    else -> {
                        isCheckingTask = false
                        hasShownWaitToast = false
                        runOnUiThread {
                            spinner.visibility = View.GONE
                            showToast(R.string.unexpected_status)
                        }
                    }
                }
            }
        })
    }




    private fun showToast(messageResId: Int) {
        runOnUiThread {
            Toast.makeText(this, getString(messageResId), Toast.LENGTH_SHORT).show()
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

    // Aplicar el idioma desde SharedPreferences
    private fun loadLocale() {
        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val language = sharedPreferences.getString("App_Lang", "es") ?: "es"
        setLocale(language)
    }

    // Establecer el idioma en la configuración
    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
    private fun animateBackgroundColor(view: View, fromColor: Int, toColor: Int) {
        val colorAnimation = ValueAnimator.ofArgb(fromColor, toColor)
        colorAnimation.duration = 1000 // duración de la transición en milisegundos
        colorAnimation.addUpdateListener { animator ->
            view.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.start()
    }

    }

