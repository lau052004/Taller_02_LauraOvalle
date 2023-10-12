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
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

@OptIn(ExperimentalCoroutinesApi::class)
class LocationService {
    @SuppressLint("MissingPermission")
    suspend fun getUserLocation(context: Context):Location?{
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        val isUserLocationPermissionGranted = true
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if(!isGPSEnabled || !isUserLocationPermissionGranted){
            return null
        }

        return suspendCancellableCoroutine { cont ->
            fusedLocationProviderClient.lastLocation.apply {
                if (isComplete){
                    if(isSuccessful){
                        cont.resume(result){}
                    } else {
                        cont.resume(null){}
                    }
                    return@suspendCancellableCoroutine
                }
                addOnSuccessListener {
                    cont.resume(it){}
                }
                addOnFailureListener{
                    cont.resume(null){}
                }
                addOnCanceledListener {
                    cont.resume(null){}
                }
            }
        }
    }
}