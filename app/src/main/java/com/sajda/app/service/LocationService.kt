package com.sajda.app.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class LocationSnapshot(
    val latitude: Double,
    val longitude: Double,
    val cityName: String
)

class LocationService(private val context: Context) {

    private val locationManager: LocationManager? by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    }

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    suspend fun getCurrentLocationSnapshot(): LocationSnapshot? {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Permission lokasi belum diberikan")
            return null
        }

        return try {
            val location = getBestLocation() ?: run {
                Log.w(TAG, "Lokasi perangkat tidak tersedia")
                return null
            }

            val cityName = reverseGeocode(location.latitude, location.longitude)
            if (cityName.isBlank()) {
                Log.w(TAG, "Reverse geocode gagal mendapatkan nama kota")
                return null
            }

            LocationSnapshot(
                latitude = location.latitude,
                longitude = location.longitude,
                cityName = cityName
            )
        } catch (error: Exception) {
            Log.e(TAG, "Gagal ambil lokasi terkini", error)
            null
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getBestLocation(): Location? {
        val lm = locationManager ?: return null

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        var bestLocation: Location? = null
        for (provider in providers) {
            if (lm.isProviderEnabled(provider)) {
                val loc = try {
                    lm.getLastKnownLocation(provider)
                } catch (e: Exception) {
                    null
                }
                if (loc != null) {
                    if (bestLocation == null || loc.time > bestLocation.time) {
                        bestLocation = loc
                    }
                }
            }
        }

        // Jika lokasi terakhir masih cukup baru (< 30 menit), gunakan langsung
        if (bestLocation != null && (System.currentTimeMillis() - bestLocation.time < 1000 * 60 * 30)) {
            Log.d(TAG, "Menggunakan lastKnownLocation")
            return bestLocation
        }

        Log.d(TAG, "Meminta fresh location dari LocationManager")
        return requestFreshLocation() ?: bestLocation
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFreshLocation(): Location? {
        val lm = locationManager ?: return null

        return suspendCancellableCoroutine { continuation ->
            val provider = when {
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                else -> null
            }

            if (provider == null) {
                if (continuation.isActive) continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    lm.removeUpdates(this)
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {
                    lm.removeUpdates(this)
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }

            continuation.invokeOnCancellation {
                lm.removeUpdates(listener)
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    lm.getCurrentLocation(
                        provider,
                        null,
                        context.mainExecutor
                    ) { location ->
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    }
                } else {
                    lm.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                }
            } catch (e: Exception) {
                Log.e(TAG, "requestSingleUpdate / getCurrentLocation gagal", e)
                lm.removeUpdates(listener)
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }
        }
    }

    private suspend fun reverseGeocode(latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale("id", "ID"))
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val address = addresses?.firstOrNull()
                listOf(
                    address?.subAdminArea,
                    address?.locality,
                    address?.adminArea,
                    address?.countryName
                ).firstOrNull { !it.isNullOrBlank() } ?: "Lokasi Saat Ini"
            } catch (e: Exception) {
                Log.e(TAG, "Geocoder error", e)
                "Lokasi Saat Ini"
            }
        }
    }

    companion object {
        private const val TAG = "LocationService"
    }
}
