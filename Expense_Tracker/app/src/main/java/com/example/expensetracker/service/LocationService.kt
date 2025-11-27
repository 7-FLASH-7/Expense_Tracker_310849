package com.example.expensetracker.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import java.util.Locale

// This package holds all your location information together
// It stores the GPS coordinates (latitude & longitude) and the readable address
data class LocationData(
    val latitude: Double,      // How far north/south you are (like 37.7749)
    val longitude: Double,     // How far east/west you are (like -122.4194)
    val address: String        // The actual street address people can read (like "123 Main St, San Francisco")
)

// This is our GPS detective!
// It finds out where you are right now and converts those numbers into a real address
class LocationService(private val context: Context) {

    // This is Google's built-in location finder
    // Think of it as your phone's GPS chip that talks to satellites
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // This is like a translator that converts GPS numbers into street addresses
    // It knows about cities, streets, and countries all over the world!
    private val geocoder = Geocoder(context, Locale.getDefault())

    // This is the main function that gets your current location
    // It's like asking "Hey phone, where am I right now?"
    suspend fun getCurrentLocation(): Result<LocationData> {
        return try {
            // First, we check if the user gave us permission to use GPS
            // It's like asking "Can I know where you are?" before tracking
            if (!hasLocationPermission()) {
                return Result.failure(SecurityException("Location permission not granted"))
            }

            // This is a safety token that lets us cancel the GPS search if it takes too long
            // Like a timeout timer for finding your location
            val cancellationToken = CancellationTokenSource()

            // Now we ask Google's location service: "Where am I?"
            // PRIORITY_HIGH_ACCURACY means we want the most precise location possible
            // (uses GPS satellites, not just wifi/cell towers)
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).await()

            // Sometimes the GPS can't find you (maybe you're indoors or underground)
            // If location is null, it means GPS couldn't get a fix
            if (location == null) {
                return Result.failure(Exception("Unable to get location"))
            }

            // Great! We got the GPS coordinates. Now let's convert them to a real address
            // For example: 37.7749, -122.4194 → "San Francisco, CA"
            val address = getAddressFromLocation(location.latitude, location.longitude)

            // Pack everything together and send it back as a success!
            Result.success(
                LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    address = address
                )
            )
        } catch (e: Exception) {
            // If anything goes wrong (no GPS signal, no internet, etc.), we return a failure
            Result.failure(e)
        }
    }

    // This magic function turns GPS numbers into a readable street address
    // It's like looking up coordinates on Google Maps to see what's actually there
    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        return try {
            // Ask the geocoder: "What address is at these coordinates?"
            // The "1" means we only want the closest match
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            // If we got results, let's build a nice address string
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]

                // We combine the street name, city, state/province, and country
                // For example: "Starbucks, San Francisco, California, USA"
                // listOfNotNull removes any parts that are null (not available)
                listOfNotNull(
                    address.featureName,    // Building or place name (like "Starbucks")
                    address.locality,       // City (like "San Francisco")
                    address.adminArea,      // State/Province (like "California")
                    address.countryName     // Country (like "USA")
                ).joinToString(", ")        // Join them with commas
            } else {
                // If geocoder couldn't find an address, just say it's unknown
                "Unknown Location"
            }
        } catch (e: Exception) {
            // If something goes wrong (like no internet to look up address),
            // we just show the raw GPS coordinates instead
            "Lat: $latitude, Lon: $longitude"
        }
    }

    // This checks if the user has given us permission to access their location
    // Think of it as checking if we have the "key" to use GPS
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        // These are the two types of location permissions we need
        // FINE = Super accurate GPS (uses satellites)
        // COARSE = Approximate location (uses wifi/cell towers)
        // We ask for both to make sure the app works in all situations
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,    // Precise GPS location
            Manifest.permission.ACCESS_COARSE_LOCATION   // Approximate location
        )
    }
}