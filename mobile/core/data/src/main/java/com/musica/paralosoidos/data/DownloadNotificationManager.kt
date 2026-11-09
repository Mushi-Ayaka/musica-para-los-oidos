package com.musica.paralosoidos.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.concurrent.ConcurrentHashMap

class DownloadNotificationManager(
    private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "musica_downloads_channel"
        const val CHANNEL_NAME = "Descargas de audio"
        const val ACTION_PAUSE_DOWNLOAD = "com.musica.paralosoidos.ACTION_PAUSE_DOWNLOAD"
        const val ACTION_RESUME_DOWNLOAD = "com.musica.paralosoidos.ACTION_RESUME_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "com.musica.paralosoidos.ACTION_CANCEL_DOWNLOAD"
        const val EXTRA_TASK_ID = "extra_task_id"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val notificationIdMap = ConcurrentHashMap<String, Int>()

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Progreso y estado de las descargas de música"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getNotificationId(taskId: String): Int {
        return notificationIdMap.getOrPut(taskId) {
            (taskId.hashCode() and 0x7FFFFFFF)
        }
    }

    fun showProgressNotification(
        taskId: String,
        title: String,
        progress: Int,
        speed: String = ""
    ) {
        val notificationId = getNotificationId(taskId)
        val pauseIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            Intent(ACTION_PAUSE_DOWNLOAD).apply {
                setPackage(context.packageName)
                putExtra(EXTRA_TASK_ID, taskId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            Intent(ACTION_CANCEL_DOWNLOAD).apply {
                setPackage(context.packageName)
                putExtra(EXTRA_TASK_ID, taskId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subtext = if (speed.isNotBlank()) "$progress% • $speed" else "$progress%"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Descargando: $title")
            .setContentText(subtext)
            .setSubText(subtext)
            .setProgress(100, progress.coerceIn(0, 100), false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_media_pause, "Pausar", pauseIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancelar", cancelIntent)

        runCatching {
            notificationManager.notify(notificationId, builder.build())
        }
    }

    fun showPausedNotification(taskId: String, title: String) {
        val notificationId = getNotificationId(taskId)
        val resumeIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 3,
            Intent(ACTION_RESUME_DOWNLOAD).apply {
                setPackage(context.packageName)
                putExtra(EXTRA_TASK_ID, taskId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            Intent(ACTION_CANCEL_DOWNLOAD).apply {
                setPackage(context.packageName)
                putExtra(EXTRA_TASK_ID, taskId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_pause)
            .setContentTitle("Descarga pausada")
            .setContentText(title)
            .setOngoing(false)
            .addAction(android.R.drawable.ic_media_play, "Reanudar", resumeIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancelar", cancelIntent)

        runCatching {
            notificationManager.notify(notificationId, builder.build())
        }
    }

    fun showCompletedNotification(taskId: String, title: String) {
        val notificationId = getNotificationId(taskId)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Descarga completada")
            .setContentText("$title guardado en tu biblioteca")
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)

        runCatching {
            notificationManager.notify(notificationId, builder.build())
        }
    }

    fun showErrorNotification(taskId: String, title: String, errorMessage: String? = null) {
        val notificationId = getNotificationId(taskId)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Error al descargar")
            .setContentText(errorMessage ?: "Fallo la descarga de $title")
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)

        runCatching {
            notificationManager.notify(notificationId, builder.build())
        }
    }

    fun showLrcAvailableNotification(trackId: String, title: String) {
        val notificationId = ("lrc_$trackId".hashCode() and 0x7FFFFFFF)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Letra disponible")
            .setContentText("Letra sincronizada encontrada para $title")
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)

        runCatching {
            notificationManager.notify(notificationId, builder.build())
        }
    }

    fun cancelNotification(taskId: String) {
        val notificationId = notificationIdMap.remove(taskId) ?: (taskId.hashCode() and 0x7FFFFFFF)
        runCatching {
            notificationManager.cancel(notificationId)
        }
    }

    fun cancelAllNotifications() {
        notificationIdMap.clear()
        runCatching {
            notificationManager.cancelAll()
        }
    }
}
