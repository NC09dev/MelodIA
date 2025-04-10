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

        // 🎵 Reproductor de audio
        mediaPlayer = MediaPlayer.create(this, R.raw.test)

        val playPauseBtn = findViewById<ImageButton>(R.id.btnPlayPause)
        seekBar = findViewById(R.id.seekBar)

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
