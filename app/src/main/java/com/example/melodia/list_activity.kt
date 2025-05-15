package com.example.melodia

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import android.graphics.Color
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.activity.enableEdgeToEdge

class Listactivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_activity)
        hideSystemUI()
        enableEdgeToEdge()

        mediaPlayer = MediaPlayer()
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val container = findViewById<LinearLayout>(R.id.songsContainer)
        val colors = listOf("#F90370", "#F5D225", "#F49194")

        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .document(userId)
            .collection("songs")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val songId = document.id
                    val songName = document.getString("title") ?: "Sin título"
                    val songUrl = document.getString("url") ?: ""

                    val item = layoutInflater.inflate(R.layout.item_song, container, false)

                    val nameText = item.findViewById<TextView>(R.id.songName)
                    val playBtn = item.findViewById<ImageView>(R.id.playButton)
                    val deleteBtn = item.findViewById<TextView>(R.id.deleteButton) // ✅ botón para eliminar
                    val layout = item.findViewById<ConstraintLayout>(R.id.songLayout)

                    layout.setBackgroundColor(Color.parseColor(colors.random()))
                    nameText.text = songName

                    playBtn.setOnClickListener {
                        if (songUrl.isNotEmpty()) {
                            reproducirDesdeUrl(songUrl)
                        } else {
                            Toast.makeText(this, "URL no válida para esta canción", Toast.LENGTH_SHORT).show()
                        }
                    }

                    deleteBtn.setOnClickListener {
                        db.collection("users")
                            .document(userId)
                            .collection("songs")
                            .document(songId)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "🎵 Canción eliminada", Toast.LENGTH_SHORT).show()
                                container.removeView(item)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "❌ Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }

                    container.addView(item)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar canciones: ${e.message}", Toast.LENGTH_LONG).show()
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

