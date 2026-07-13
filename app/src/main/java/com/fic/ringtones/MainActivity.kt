package com.fic.ringtones

import android.content.ContentValues
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fic.ringtones.ui.theme.RingtonesTheme

class MainActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    private fun reproducirAudio(audioResId: Int) {
        detenerAudio()
        mediaPlayer = MediaPlayer.create(this, audioResId)?.apply {
            setOnCompletionListener { player ->
                player.release()
                if (mediaPlayer === player) {
                    mediaPlayer = null
                }
            }
            start()
        }
    }

    private fun detenerAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }

    private fun descargarAudio(audioResId: Int, fileName: String): Boolean {
        return try {
            val resolver = contentResolver
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/RingtonesApp"
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val itemUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return false

            resolver.openOutputStream(itemUri)?.use { output ->
                resources.openRawResource(audioResId).use { input ->
                    input.copyTo(output)
                }
            } ?: return false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val completedValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }
                resolver.update(itemUri, completedValues, null, null)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RingtonesTheme {
                var reproduciendoNombre by rememberSaveable { mutableStateOf<String?>(null) }

                val ringtones = listOf(
                    RingtoneItem(R.string.ringtone_1, R.raw.tono1, "tono1.mp3"),
                    RingtoneItem(R.string.ringtone_2, R.raw.tono2, "tono2.mp3"),
                    RingtoneItem(R.string.ringtone_3, R.raw.tono3, "tono3.mp3"),
                    RingtoneItem(R.string.ringtone_4, R.raw.tono4, "tono4.mp3"),
                    RingtoneItem(R.string.ringtone_5, R.raw.tono5, "tono5.mp3"),
                    RingtoneItem(R.string.ringtone_6, R.raw.tono6, "tono6.mp3"),
                    RingtoneItem(R.string.ringtone_7, R.raw.tono7, "tono7.mp3"),
                    RingtoneItem(R.string.ringtone_8, R.raw.tono8, "tono8.mp3"),
                    RingtoneItem(R.string.ringtone_9, R.raw.tono9, "tono9.mp3"),
                    RingtoneItem(R.string.ringtone_10, R.raw.tono10, "tono10.mp3")
                )

                RingtonesScreen(
                    ringtones = ringtones,
                    reproduciendoNombre = reproduciendoNombre,
                    onPlayClick = { ringtone ->
                        val nombre = getString(ringtone.nombreResId)
                        reproducirAudio(ringtone.audioResId)
                        reproduciendoNombre = nombre
                        Toast.makeText(
                            this,
                            getString(R.string.toast_playing, nombre),
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onDownloadClick = { ringtone ->
                        val nombre = getString(ringtone.nombreResId)
                        val descargaCorrecta = descargarAudio(ringtone.audioResId, ringtone.fileName)
                        val mensaje = if (descargaCorrecta) {
                            getString(R.string.toast_download_ok, nombre)
                        } else {
                            getString(R.string.toast_download_error)
                        }
                        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                    },
                    onStopClick = {
                        detenerAudio()
                        reproduciendoNombre = null
                        Toast.makeText(
                            this,
                            getString(R.string.toast_stopped),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        detenerAudio()
    }

    override fun onDestroy() {
        super.onDestroy()
        detenerAudio()
    }
}

data class RingtoneItem(
    val nombreResId: Int,
    val audioResId: Int,
    val fileName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RingtonesScreen(
    ringtones: List<RingtoneItem>,
    reproduciendoNombre: String?,
    onPlayClick: (RingtoneItem) -> Unit,
    onDownloadClick: (RingtoneItem) -> Unit,
    onStopClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(R.string.screen_title))
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.screen_subtitle),
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = reproduciendoNombre?.let {
                    stringResource(R.string.status_playing, it)
                } ?: stringResource(R.string.status_stopped),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(ringtones) { ringtone ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(ringtone.nombreResId),
                                style = MaterialTheme.typography.titleMedium
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onPlayClick(ringtone) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(stringResource(R.string.action_play))
                                }

                                OutlinedButton(
                                    onClick = { onDownloadClick(ringtone) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(stringResource(R.string.action_download))
                                }
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onStopClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Red
                )
            ) {
                Text(stringResource(R.string.action_stop_audio))
            }
        }
    }
}