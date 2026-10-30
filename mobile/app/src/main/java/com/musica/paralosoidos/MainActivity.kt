package com.musica.paralosoidos

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.musica.paralosoidos.playback.PlaybackService
import com.musica.paralosoidos.ui.MusicaRoot
import com.musica.paralosoidos.ui.theme.MusicaTheme
import com.google.common.util.concurrent.ListenableFuture
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController by mutableStateOf<MediaController?>(null)
    private var audioPermissionGranted by mutableStateOf(false)
    private val audioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            audioPermissionGranted = granted
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        audioPermissionGranted = hasAudioPermission()

        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync().also { future ->
            future.addListener(
                { mediaController = future.get() },
                ContextCompat.getMainExecutor(this)
            )
        }

        val container = (application as MusicaApp).container
        setContent {
            val themeSettings by container.preferences.themeSettings.collectAsStateWithLifecycle(
                initialValue = com.musica.paralosoidos.data.ThemeSettingsState()
            )
            MusicaTheme(settings = themeSettings) {
                MusicaRoot(
                    container = container,
                    controller = mediaController,
                    hasAudioPermission = { audioPermissionGranted },
                    requestAudioPermission = { audioPermissionLauncher.launch(audioPermission()) }
                )
            }
        }
    }

    private fun audioPermission(): String =
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE

    private fun hasAudioPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, audioPermission()) ==
            PackageManager.PERMISSION_GRANTED

    override fun onDestroy() {
        controllerFuture?.let(MediaController::releaseFuture)
        mediaController = null
        super.onDestroy()
    }
}
