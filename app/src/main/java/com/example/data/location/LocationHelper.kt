package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume

data class ResolvedLocation(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val countryCode: String,
    val timezoneId: String
)

class LocationHelper(private val context: Context) {

    private val fusedClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentGpsLocation(): Result<ResolvedLocation> = withContext(Dispatchers.IO) {
        try {
            val loc = getFreshOrLastLocation()
                ?: return@withContext Result.failure(IllegalStateException("تعذر تحديد موقع GPS حالياً"))
            val geocoded = reverseGeocode(loc.latitude, loc.longitude)
            Result.success(
                ResolvedLocation(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    cityName = geocoded.first ?: "موقعي الحالي",
                    countryCode = geocoded.second ?: "",
                    timezoneId = TimeZone.getDefault().id
                )
            )
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getFreshOrLastLocation(): android.location.Location? {
        val current = suspendCancellableCoroutine<android.location.Location?> { cont ->
            val cts = CancellationTokenSource()
            cont.invokeOnCancellation { cts.cancel() }
            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    if (cont.isActive) cont.resume(location)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
        }
        if (current != null) return current

        return suspendCancellableCoroutine { cont ->
            fusedClient.lastLocation
                .addOnSuccessListener { location ->
                    if (cont.isActive) cont.resume(location)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
        }
    }

    suspend fun searchCityByName(query: String): List<ResolvedLocation> = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent() || query.isBlank()) return@withContext emptyList()
        try {
            val geocoder = Geocoder(context, Locale("ar"))
            val addresses = getFromLocationNameCompat(geocoder, query, 5)
            addresses.mapNotNull { addr ->
                val name = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: addr.featureName ?: return@mapNotNull null
                val country = addr.countryName?.let { "$name، $it" } ?: name
                ResolvedLocation(
                    latitude = addr.latitude,
                    longitude = addr.longitude,
                    cityName = country,
                    countryCode = addr.countryCode ?: "",
                    timezoneId = TimeZone.getDefault().id
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private suspend fun reverseGeocode(lat: Double, lng: Double): Pair<String?, String?> {
        if (!Geocoder.isPresent()) return null to null
        return try {
            val geocoder = Geocoder(context, Locale("ar"))
            val list = getFromLocationCompat(geocoder, lat, lng, 1)
            val addr = list.firstOrNull()
            val city = addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea ?: addr?.countryName
            city to addr?.countryCode
        } catch (_: Throwable) {
            null to null
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun getFromLocationCompat(
        geocoder: Geocoder,
        lat: Double,
        lng: Double,
        maxResults: Int
    ): List<Address> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(lat, lng, maxResults, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (cont.isActive) cont.resume(addresses)
                    }

                    override fun onError(errorMessage: String?) {
                        if (cont.isActive) cont.resume(emptyList())
                    }
                })
            }
        } else {
            geocoder.getFromLocation(lat, lng, maxResults).orEmpty()
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun getFromLocationNameCompat(
        geocoder: Geocoder,
        query: String,
        maxResults: Int
    ): List<Address> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocationName(query, maxResults, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (cont.isActive) cont.resume(addresses)
                    }

                    override fun onError(errorMessage: String?) {
                        if (cont.isActive) cont.resume(emptyList())
                    }
                })
            }
        } else {
            geocoder.getFromLocationName(query, maxResults).orEmpty()
        }
    }
}
