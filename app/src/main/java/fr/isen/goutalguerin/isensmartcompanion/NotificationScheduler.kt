package fr.isen.goutalguerin.isensmartcompanion

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.util.Calendar

object NotificationScheduler {
    private const val NOTIFICATION_CHANNEL_ID = "event_reminders"

    // Surcharge pour AgendaEvent (utilisé par AgendaScreen)
    fun scheduleNotification(context: Context, event: AgendaEvent) {
        if (!event.reminderEnabled) return

        val eventTime = event.date.clone() as Calendar
        event.startTime?.let { startTime: String ->
            val (hour, minute) = startTime.split(":").map { it.toInt() }
            eventTime.set(Calendar.HOUR_OF_DAY, hour)
            eventTime.set(Calendar.MINUTE, minute)
            eventTime.set(Calendar.SECOND, 0)
        }
        val delay = eventTime.timeInMillis - System.currentTimeMillis()
        val finalDelay = if (delay > 0) delay else 10000L // Minimum 10 secondes si l'heure est passée

        Handler(Looper.getMainLooper()).postDelayed({
            showNotification(context, event)
        }, finalDelay)
    }

    // Surcharge pour Event (utilisé par EventDetailActivity)
    fun scheduleNotification(context: Context, event: Event) {
        Handler(Looper.getMainLooper()).postDelayed({
            showNotification(context, event)
        }, 10000) // 10 secondes comme dans votre code original
    }

    private fun showNotification(context: Context, event: AgendaEvent) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Note : Si vous voulez rediriger vers une activité spécifique avec les détails de l'événement,
            // AgendaEvent devra être Parcelable ou Serializable, ou passer des données via extras
        }

        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(
            context,
            event.title.hashCode(),
            intent,
            pendingIntentFlags
        )

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(event.title)
            .setContentText("${if (event.isCourse) "Cours" else "Événement"} ${event.getFormattedTimeRange()}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(event.title.hashCode(), builder.build())
        }
    }

    private fun showNotification(context: Context, event: Event) {
        val intent = Intent(context, EventDetailActivity::class.java).apply {
            putExtra("event", event)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(
            context,
            event.id.hashCode(),
            intent,
            pendingIntentFlags
        )

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(event.title)
            .setContentText(event.description)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(event.id.hashCode(), builder.build())
        }
    }
}