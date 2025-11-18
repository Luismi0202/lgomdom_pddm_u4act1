package com.example.myapplication

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

suspend fun playYouTubeUrl(exoPlayer: ExoPlayer, videoId: String) {
    try {
        val url = "https://www.youtube.com/watch?v=$videoId"
        // Usa la URL directa en formato HLS/DASH
        val mediaItem = MediaItem.fromUri("https://manifest.googlevideo.com/api/manifest/hls_variant/$videoId")
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
