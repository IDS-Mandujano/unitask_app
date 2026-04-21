package com.example.unitask_app.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.unitask_app.R
import com.example.unitask_app.data.local.TokenManager
import com.example.unitask_app.data.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.random.Random
import javax.inject.Inject

@AndroidEntryPoint
class UniTaskFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val REMINDER_CHANNEL_ID = "unitask_reminders"
    }

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        CoroutineScope(Dispatchers.IO).launch {
            tokenManager.saveFcmToken(token)

            val jwt = tokenManager.getToken().firstOrNull()
            if (!jwt.isNullOrBlank()) {
                try {
                    val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                    userRepository.registerDeviceToken(
                        deviceToken = token,
                        deviceId = if (deviceId.isNullOrBlank()) Build.MODEL ?: "" else deviceId
                    )
                } catch (_: Exception) {
                    // Evita crashear si falla internet o backend temporalmente.
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "UniTask"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "Tienes una nueva notificación"

        showLocalNotification(title, body)
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            "Recordatorios UniTask",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Recordatorios de tareas y notificaciones importantes"
        }
        manager.createNotificationChannel(channel)
    }

    private fun showLocalNotification(title: String, body: String) {
        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return

        val notification = NotificationCompat.Builder(this, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(Random.nextInt(), notification)
    }
}
