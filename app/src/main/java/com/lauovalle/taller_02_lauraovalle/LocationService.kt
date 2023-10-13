package com.lauovalle.taller_02_lauraovalle

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

@OptIn(ExperimentalCoroutinesApi::class)
class LocationService {

    @SuppressLint("MissingPermission")
    suspend fun getRoute(context: Context, mMap: GoogleMap) {
        var fusedLocationClient: FusedLocationProviderClient
        var locationCallback: LocationCallback
        var polylineOptions = PolylineOptions()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    // Obtén la nueva ubicación
                    val latLng = LatLng(location.latitude, location.longitude)

                    // Agrega el punto a PolylineOptions
                    polylineOptions.add(latLng)

                    // Dibuja el Polyline en el mapa
                    mMap.addPolyline(polylineOptions)

                    // Mueve la cámara al nuevo punto
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // Intervalo de actualización de ubicación en milisegundos
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // Verifica la configuración de ubicación
        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // Configuración de ubicación aceptada, comienza la actualización
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }
}