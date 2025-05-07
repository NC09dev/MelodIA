package com.example.melodia

import android.media.MediaPlayer

object AudioPlayerManager {
    var mediaPlayer: MediaPlayer? = null

    fun stopAndRelease() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
    }
}
