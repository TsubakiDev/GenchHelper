package dev.tsubaki.genchelper.utilities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import java.util.concurrent.atomic.AtomicInteger

@SuppressLint("StaticFieldLeak")
object NotificationUtils {

    private val notificationId = AtomicInteger(1000)
    private const val DEFAULT_CHANNEL_ID = "ghelper_default_channel"

    class Builder(private val context: Context) {
        private var priority: Int = NotificationCompat.PRIORITY_DEFAULT
        private var config = NotificationConfig()

        fun setTitle(title: String) = apply { config = config.copy(title = title) }
        fun setContent(content: String) = apply { config = config.copy(content = content) }
        fun setSmallIcon(@DrawableRes resId: Int) = apply { config = config.copy(smallIcon = resId) }
        fun setLargeIcon(bitmap: Bitmap?) = apply { config = config.copy(largeIcon = bitmap) }
        fun setChannel(channelId: String, channelName: String, importance: Int) = apply {
            config = config.copy(channelConfig = ChannelConfig(channelId, channelName, importance))
        }
        fun setPriority(priority: Int) = apply {
            this.priority = when {
                priority < NotificationCompat.PRIORITY_MIN -> NotificationCompat.PRIORITY_MIN
                priority > NotificationCompat.PRIORITY_MAX -> NotificationCompat.PRIORITY_MAX
                else -> priority
            }
            config = config.copy(priority = this.priority)
        }
        fun setAutoCancel(autoCancel: Boolean) = apply { config = config.copy(autoCancel = autoCancel) }
        fun setTargetActivity(activityClass: Class<out Activity>?) = apply { config = config.copy(targetActivity = activityClass) }
        fun setCustomAction(pendingIntent: PendingIntent?) = apply { config = config.copy(pendingIntent = pendingIntent) }
        fun setProgress(progress: Int, max: Int, indeterminate: Boolean = false) = apply {
            config = config.copy(showProgress = true, progress = progress, max = max, indeterminate = indeterminate)
        }

        fun show() {
            require(config.smallIcon != 0) { "Small icon must be set" }

            val manager = context.notificationManager
            val safeContext = context.applicationContext
            val channelId = config.channelConfig?.id ?: DEFAULT_CHANNEL_ID

            config.channelConfig?.let { channel ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    manager.createChannel(channel)
                }
            } ?: run {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    manager.safeGetChannel(DEFAULT_CHANNEL_ID) ?: createDefaultChannel(manager)
                }
            }

            NotificationCompat.Builder(safeContext, channelId).apply {
                config.title?.let { setContentTitle(it) }
                config.content?.let { setContentText(it) }
                setSmallIcon(config.smallIcon)
                config.largeIcon?.let { setLargeIcon(it) }
                priority = config.priority
                setAutoCancel(config.autoCancel)
                setContentIntent(config.pendingIntent ?: createPendingIntent(safeContext))
                setShowWhen(true)

                if (config.showProgress) {
                    setProgress(config.max, config.progress, config.indeterminate)
                    setOngoing(true)
                }
            }.build().let {
                manager.notify(notificationId.getAndIncrement(), it)
            }
        }

        private fun createPendingIntent(context: Context): PendingIntent? {
            return config.targetActivity?.let { activityClass ->
                Intent(context, activityClass).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }.let { intent ->
                    PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun createDefaultChannel(manager: NotificationManager) {
            NotificationChannel(
                DEFAULT_CHANNEL_ID,
                "默认通知",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "默认通知"
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                manager.createNotificationChannel(this)
            }
        }
    }

    fun Context.showQuickNotification(
        title: String,
        content: String,
        @DrawableRes iconRes: Int
    ) {
        Builder(this)
            .setTitle(title)
            .setContent(content)
            .setSmallIcon(iconRes)
            .show()
    }

    fun cancelAll(context: Context) = context.notificationManager.cancelAll()
    fun cancel(context: Context, id: Int) = context.notificationManager.cancel(id)

    private data class NotificationConfig(
        val title: String? = null,
        val content: String? = null,
        @DrawableRes val smallIcon: Int = 0,
        val largeIcon: Bitmap? = null,
        val channelConfig: ChannelConfig? = null,
        val priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        val autoCancel: Boolean = true,
        val targetActivity: Class<out Activity>? = null,
        val pendingIntent: PendingIntent? = null,
        val showProgress: Boolean = false,
        val progress: Int = 0,
        val max: Int = 100,
        val indeterminate: Boolean = false
    )

    private data class ChannelConfig(
        val id: String,
        val name: String,
        val importance: Int
    )

    private val Context.notificationManager: NotificationManager
        get() = getSystemService()!!

    @RequiresApi(Build.VERSION_CODES.O)
    private fun NotificationManager.safeGetChannel(channelId: String): NotificationChannel? {
        return try {
            getNotificationChannel(channelId)
        } catch (e: Exception) {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun NotificationManager.createChannel(config: ChannelConfig) {
        safeGetChannel(config.id) ?: NotificationChannel(
            config.id,
            config.name,
            config.importance
        ).also { createNotificationChannel(it) }
    }
}