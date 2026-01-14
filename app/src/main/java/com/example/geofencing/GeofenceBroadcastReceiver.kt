package com.example.geofencing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null) {
            Log.e(TAG, "GeofencingEvent is null")
            return
        }

        // Check for errors
        if (geofencingEvent.hasError()) {
            val errorMessage = getErrorString(geofencingEvent.errorCode)
            Log.e(TAG, "Geofencing error: $errorMessage")
            return
        }

        // Get the transition type
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Get triggering location
        val location = geofencingEvent.triggeringLocation
        Log.d(TAG, "Triggering location: ${location?.latitude}, ${location?.longitude}")

        // Handle transition
        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.d(TAG, "Entered geofence")
                NotificationHelper(context).showGeofenceNotification(
                    title = context.getString(R.string.geofence_enter_title),
                    message = context.getString(R.string.geofence_enter_message)
                )
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.d(TAG, "Exited geofence")
                NotificationHelper(context).showGeofenceNotification(
                    title = context.getString(R.string.geofence_exit_title),
                    message = context.getString(R.string.geofence_exit_message)
                )
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                Log.d(TAG, "Dwelling in geofence")
                // Optional: handle dwell transition
                // It can also be used instead of enter/exit transitions to prevent unnecessary
                // notification spam when user only passes by a geofence
            }
            else -> {
                Log.e(TAG, "Unknown geofence transition: $geofenceTransition")
            }
        }
    }

    private fun getErrorString(errorCode: Int): String {
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> "Geofence service is not available now"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "Too many geofences (max 100)"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "Too many pending intents"
            else -> "Unknown error: $errorCode"
        }
    }

    companion object {
        private const val TAG = "GeofenceReceiver"
    }
}