package com.example.geofencing;

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import java.util.Locale
import com.example.geofencing.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var geofenceManager: GeofenceManager
    private lateinit var notificationHelper: NotificationHelper

    private var locationUpdatesStarted = false
    private var currentRadius = 100f
    private var geofenceActive = false
    private var geofenceLat: Double? = null
    private var geofenceLon: Double? = null

    // Permission launchers
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                checkBackgroundLocationPermission()
            }
            else -> {
                Toast.makeText(
                    this,
                    getString(R.string.location_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private val backgroundLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                this,
                getString(R.string.background_location_permission_denied),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                this,
                getString(R.string.notification_permission_denied),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val dp16 = (16 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left + dp16,
                systemBars.top,
                systemBars.right + dp16,
                systemBars.bottom
            )
            insets
        }

        // Initialize
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geofenceManager = GeofenceManager(this)
        notificationHelper = NotificationHelper(this)

        // Create notification channel
        notificationHelper.createNotificationChannel()

        // Check permissions
        checkPermissions()

        // Setup UI
        setupSlider()
        setupButtons()

        // Get current location
        getCurrentLocation()
        startLocationUpdates()
    }

    private fun checkPermissions() {
        when {
            hasLocationPermissions() -> {
                checkBackgroundLocationPermission()
                checkNotificationPermission()
            }
            else -> {
                requestLocationPermissions()
            }
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun checkBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                backgroundLocationPermissionLauncher.launch(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    private fun setupSlider() {
        binding.radiusSlider.addOnChangeListener { _, value, _ ->
            currentRadius = value
            binding.radiusValueText.text = getString(R.string.radius_format, value.toInt())
        }
    }

    private fun setupButtons() {
        binding.addGeofenceButton.setOnClickListener {
            if (!hasLocationPermissions()) {
                requestLocationPermissions()
                return@setOnClickListener
            }
            addGeofence()
        }

        binding.removeGeofenceButton.setOnClickListener {
            removeGeofences()
        }

        binding.testNotificationButton.setOnClickListener {
            notificationHelper.showTestNotification()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (!hasLocationPermissions()) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                updateLocationUI(location.latitude, location.longitude)
            } else {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { freshLocation ->
                    if (freshLocation != null) {
                        updateLocationUI(freshLocation.latitude, freshLocation.longitude)
                    } else {
                        binding.locationText.text = getString(R.string.location_unavailable)
                    }
                }.addOnFailureListener {
                    binding.locationText.text = getString(R.string.location_unavailable)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!hasLocationPermissions() || locationUpdatesStarted) return

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000
        )
            .setMinUpdateIntervalMillis(2000)
            .setWaitForAccurateLocation(true)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                updateLocationUI(location.latitude, location.longitude)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )

        locationUpdatesStarted = true
    }

    private fun stopLocationUpdates() {
        if (locationUpdatesStarted) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            locationUpdatesStarted = false
        }
    }

    private fun updateLocationUI(lat: Double, lon: Double) {
        val locationText = getString(R.string.location_format, lat, lon)
        binding.locationText.text = locationText
    }

    private fun addGeofence() {
        if (!hasLocationPermissions()) {
            Toast.makeText(
                this,
                getString(R.string.location_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    geofenceManager.addGeofence(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        radius = currentRadius,
                        onSuccess = {
                            geofenceActive = true
                            geofenceLat = location.latitude
                            geofenceLon = location.longitude
                            updateStatus()
                            Toast.makeText(
                                this,
                                getString(R.string.geofence_added),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = { exception ->
                            Toast.makeText(
                                this,
                                getString(R.string.geofence_error, exception.message),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.location_unavailable),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (_: SecurityException) {
            Toast.makeText(
                this,
                getString(R.string.location_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun removeGeofences() {
        geofenceManager.removeGeofences(
            onSuccess = {
                geofenceActive = false
                updateStatus()
                Toast.makeText(
                    this,
                    getString(R.string.geofence_removed),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onFailure = { exception ->
                Toast.makeText(
                    this,
                    getString(R.string.geofence_error, exception.message),
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    private fun updateStatus() {
        binding.statusText.text = if (geofenceActive && geofenceLat != null && geofenceLon != null) {
            val shortLat = String.format(Locale.US, "%.5f", geofenceLat)
            val shortLon = String.format(Locale.US, "%.5f", geofenceLon)
            getString(R.string.geofence_active_with_coords, currentRadius.toInt(), shortLat, shortLon)
        } else {
            getString(R.string.no_geofences)
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }
}