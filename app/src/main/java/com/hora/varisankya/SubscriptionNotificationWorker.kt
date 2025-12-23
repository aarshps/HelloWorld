package com.hora.varisankya

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SubscriptionNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return Result.success()
        val firestore = FirebaseFirestore.getInstance()

        try {
            val snapshots = firestore.collection("users")
                .document(userId)
                .collection("subscriptions")
                .get()
                .await()

            val subscriptions = snapshots.toObjects(Subscription::class.java)
            val today = Calendar.getInstance()
            // Reset time to start of day
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            val sevenDaysLater = Calendar.getInstance()
            sevenDaysLater.time = today.time
            sevenDaysLater.add(Calendar.DAY_OF_YEAR, 7)

            var notificationCount = 0

            subscriptions.forEach { sub ->
                sub.dueDate?.let { dueDate ->
                    val dueCal = Calendar.getInstance()
                    dueCal.time = dueDate
                    // Reset time
                    dueCal.set(Calendar.HOUR_OF_DAY, 0)
                    dueCal.set(Calendar.MINUTE, 0)
                    dueCal.set(Calendar.SECOND, 0)
                    dueCal.set(Calendar.MILLISECOND, 0)

                    if (dueCal.timeInMillis >= today.timeInMillis && 
                        dueCal.timeInMillis <= sevenDaysLater.timeInMillis) {
                        
                        val diff = dueCal.timeInMillis - today.timeInMillis
                        val daysLeft = TimeUnit.MILLISECONDS.toDays(diff)
                        
                        sendNotification(sub, daysLeft.toInt())
                        notificationCount++
                    }
                }
            }

            if (notificationCount > 0) {
                sendSummaryNotification(notificationCount)
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    private fun sendNotification(subscription: Subscription, daysLeft: Int) {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        createNotificationChannel()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext, 
            subscription.id.hashCode(), 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        )

        // Concise title for better visibility
        val title = "Due"
        val message = when (daysLeft) {
            0 -> "${subscription.name} is due today!"
            1 -> "${subscription.name} is due tomorrow!"
            else -> "${subscription.name} is due in $daysLeft days."
        }

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup(GROUP_KEY_SUBSCRIPTIONS) // Add to group

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(subscription.id.hashCode(), builder.build())
        }
    }

    private fun sendSummaryNotification(count: Int) {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val summaryNotification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setStyle(NotificationCompat.InboxStyle()
                .setSummaryText("$count subscriptions due soon"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup(GROUP_KEY_SUBSCRIPTIONS)
            .setGroupSummary(true) // Set as summary
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(SUMMARY_ID, summaryNotification)
        }
    }

    private fun createNotificationChannel() {
        val name = "Subscription Reminders"
        val descriptionText = "Daily reminders for upcoming subscriptions"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "subscription_reminders"
        const val GROUP_KEY_SUBSCRIPTIONS = "com.hora.varisankya.SUBSCRIPTION_UPDATES"
        const val SUMMARY_ID = 0
    }
}