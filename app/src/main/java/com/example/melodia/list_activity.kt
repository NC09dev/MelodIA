package com.example.melodia

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import android.graphics.Color
import androidx.activity.enableEdgeToEdge

class Listactivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_activity)
        hideSystemUI()
        enableEdgeToEdge()

        mediaPlayer = MediaPlayer()

        val container = findViewById<LinearLayout>(R.id.songsContainer)
        val prefs = getSharedPreferences("saved_songs", MODE_PRIVATE)
        val allSongs = prefs.all

        val colors = listOf("#F90370", "#F5D225", "#F49194")

        for ((name, url) in allSongs) {
            val item = layoutInflater.inflate(R.layout.item_song, container, false)

            val nameText = item.findViewById<TextView>(R.id.songName)
            val playBtn = item.findViewById<ImageView>(R.id.playButton)
            val layout = item.findViewById<ConstraintLayout>(R.id.songLayout)

            layout.setBackgroundColor(Color.parseColor(colors.random()))
            nameText.text = name.toString()

            playBtn.setOnClickListener {
                reproducirDesdeUrl(url.toString())
            }

            container.addView(item)
        }
    }

    private fun reproducirDesdeUrl(audioUrl: String) {
        try {
            if (::mediaPlayer.isInitialized) {
                mediaPlayer.reset()
            } else {
                mediaPlayer = MediaPlayer()
            }

            mediaPlayer.setDataSource(audioUrl)
            mediaPlayer.setOnPreparedListener {
                mediaPlayer.start()
                Toast.makeText(this, "🎵 Reproduciendo canción", Toast.LENGTH_SHORT).show()
            }
            mediaPlayer.setOnCompletionListener {
                Toast.makeText(this, "✅ Reproducción terminada", Toast.LENGTH_SHORT).show()
            }
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
        if (::mediaPlayer.isInitialized) mediaPlayer.release()
    }


}
