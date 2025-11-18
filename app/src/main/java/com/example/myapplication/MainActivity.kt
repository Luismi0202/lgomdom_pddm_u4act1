package com.example.myapplication

import android.Manifest
import android.content.Context
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.io.File

// URLs para audio y video remotos
private const val REMOTE_AUDIO_URL = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3"
private const val REMOTE_VIDEO_URL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

class MainActivity : ComponentActivity() {
    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0
    private lateinit var exoPlayerAudio: ExoPlayer
    private lateinit var exoPlayerVideo: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        soundPool = SoundPool.Builder().setMaxStreams(1).build()
        soundId = soundPool.load(this, R.raw.short_sound, 1)

        exoPlayerAudio = ExoPlayer.Builder(this).build()
        exoPlayerVideo = ExoPlayer.Builder(this).build()

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MediaScreen(
                        context = this@MainActivity,
                        soundPool = soundPool,
                        soundId = soundId,
                        exoPlayerAudio = exoPlayerAudio,
                        exoPlayerVideo = exoPlayerVideo,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
        exoPlayerAudio.release()
        exoPlayerVideo.release()
    }
}

@Composable
fun MediaScreen(
    context: Context,
    soundPool: SoundPool,
    soundId: Int,
    exoPlayerAudio: ExoPlayer,
    exoPlayerVideo: ExoPlayer,
    modifier: Modifier = Modifier
) {
    var showVideoPlayer by remember { mutableStateOf(false) }
    var showAudioPlayer by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                capturedImageUri = tempImageUri
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                capturedImageUri = uri
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val file = File.createTempFile("img_", ".jpg", context.cacheDir)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                tempImageUri = uri
                takePictureLauncher.launch(uri)
            }
        }
    )

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                galleryLauncher.launch("image/*")
            }
        }
    )

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Seleccionar imagen") },
            text = { Text("Elige una opción para obtener la imagen.") },
            confirmButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Text("Tomar Foto")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    } else {
                        galleryLauncher.launch("image/*")
                    }
                }) {
                    Text("Galería")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Multimedia: Sonidos, Audio, Video",
            style = MaterialTheme.typography.headlineSmall
        )

        // Sonido corto (SoundPool)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Sonido corto (SoundPool)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { soundPool.play(soundId, 1f, 1f, 1, 0, 1f) }) {
                        Text("Play")
                    }
                    Button(onClick = { soundPool.stop(soundId) }) {
                        Text("Stop")
                    }
                }
            }
        }

        // Pista de audio largo (ExoPlayer)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Pista de audio larga (ExoPlayer)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val mediaItem = MediaItem.fromUri(REMOTE_AUDIO_URL)
                            exoPlayerAudio.setMediaItem(mediaItem)
                            exoPlayerAudio.prepare()
                            exoPlayerAudio.play()
                            showAudioPlayer = true
                        }
                    ) {
                        Text("Play")
                    }
                    Button(onClick = { exoPlayerAudio.pause() }) { Text("Pause") }
                    Button(onClick = { exoPlayerAudio.stop() }) { Text("Stop") }
                }

                if (showAudioPlayer) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayerAudio
                                useController = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                }
            }
        }

        // Vídeo (ExoPlayer)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Video remoto (ExoPlayer)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val mediaItem = MediaItem.fromUri(REMOTE_VIDEO_URL)
                            exoPlayerVideo.setMediaItem(mediaItem)
                            exoPlayerVideo.prepare()
                            exoPlayerVideo.play()
                            showVideoPlayer = true
                        }
                    ) {
                        Text("Play")
                    }
                    Button(onClick = { exoPlayerVideo.pause() }) { Text("Pause") }
                    Button(onClick = { exoPlayerVideo.stop() }) { Text("Stop") }
                }

                if (showVideoPlayer) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayerVideo
                                useController = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }
            }
        }

        // Cámara
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Captura de Cámara", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showImageSourceDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Seleccionar/Tomar Foto")
                }

                if (capturedImageUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Foto capturada:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        painter = rememberAsyncImagePainter(model = capturedImageUri),
                        contentDescription = "Foto capturada",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
