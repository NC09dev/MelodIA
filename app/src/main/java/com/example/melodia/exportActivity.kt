package com.example.melodia

import android.animation.ValueAnimator
import android.app.DownloadManager
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
import java.util.Locale
import android.content.Context

class exportActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_activity)
        hideSystemUI()
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener { finish() }

        val container = findViewById<LinearLayout>(R.id.songsContainer)

        // Mapa de colores: fondo -> texto
        val colorPairs = mapOf(
            "#F49194" to "#FFDF8E",
            "#F5B926" to "#BC724F",
            "#F5D225" to "#D98737"
        )

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

                    val item = layoutInflater.inflate(R.layout.export_song, container, false)
                    val nameText = item.findViewById<TextView>(R.id.songName)
                    val downloadBtn = item.findViewById<ImageView>(R.id.downloadButton)
                    val layout = item.findViewById<ConstraintLayout>(R.id.songLayout)

                    // Aplicar color de fondo y color de texto combinados
                    val (backgroundColor, textColor) = colorPairs.entries.random()
                    layout.setBackgroundColor(Color.parseColor(backgroundColor))
                    nameText.setTextColor(Color.parseColor(textColor))
                    nameText.text = songName

                    downloadBtn.setOnClickListener {
                        if (songUrl.isNotEmpty()) {
                            try {
                                val request = DownloadManager.Request(android.net.Uri.parse(songUrl))
                                    .setTitle("Descargando canción")
                                    .setDescription("Guardando $songName.mp3")
                                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    .setDestinationInExternalPublicDir(
                                        android.os.Environment.DIRECTORY_DOWNLOADS,
                                        "$songName.mp3"
                                    )
                                    .setAllowedOverMetered(true)
                                    .setAllowedOverRoaming(true)

                                val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                downloadManager.enqueue(request)

                                Toast.makeText(this, "🎶 Descargando \"$songName\"", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(this, "❌ Error al iniciar descarga: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this, "URL no válida para esta canción", Toast.LENGTH_SHORT).show()
                        }
                    }

                    container.addView(item)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar canciones: ${e.message}", Toast.LENGTH_LONG).show()
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

    // --- Cambio de idioma ---
    private fun loadLocale() {
        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val language = sharedPreferences.getString("App_Lang", "es") ?: "es"
        setLocale(language)
    }

    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}
