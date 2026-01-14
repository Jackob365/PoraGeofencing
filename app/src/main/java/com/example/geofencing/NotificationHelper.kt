package com.example.geofencing

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
class NotificationHelper(private val context: Context) {

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.geofence_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.geofence_channel_description)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showGeofenceNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID++, notification)
        } catch (e: SecurityException) {
            android.util.Log.e(TAG, "Notification permission not granted", e)
        }
    }

    fun showTestNotification() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)


        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            showGeofenceNotification(
                title = context.getString(R.string.test_notification_title),
                message = context.getString(R.string.location_permission_denied)
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val message = if (location != null) {
                context.getString(R.string.test_notification_message, location.latitude, location.longitude)
            } else {
                context.getString(R.string.location_unavailable)
            }

            showGeofenceNotification(
                title = context.getString(R.string.test_notification_title),
                message = message
            )
        }.addOnFailureListener {
            showGeofenceNotification(
                title = context.getString(R.string.test_notification_title),
                message = context.getString(R.string.location_unavailable)
            )
        }
    }

    companion object {
        private const val CHANNEL_ID = "geofence_channel"
        private var NOTIFICATION_ID = 1
        private const val TAG = "NotificationHelper"
    }
}