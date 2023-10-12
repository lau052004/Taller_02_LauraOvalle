package com.lauovalle.taller_02_lauraovalle

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.lauovalle.taller_02_lauraovalle.databinding.ActivityMapsBinding
import kotlinx.coroutines.launch
import java.util.logging.Logger

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    // GLOBALES ----------------------------------------------------------------
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mMap: GoogleMap
    private val locationService:LocationService = LocationService()
    val TAG = MapsActivity::javaClass


    companion object {
        val REQUEST_CODE_LOCATION = 0
    }

    // ON_CREATE ----------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // ON_CREATE ----------------------------------------------------------------
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.zoomTo(20f))
        mMap.uiSettings.setAllGesturesEnabled(true)
        /// Add UI controls
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        enableLocation()

        lifecycleScope.launch {
            val result = locationService.getUserLocation(this@MapsActivity)
            if(result!=null)
            {
                val ubicacion = LatLng(result.latitude, result.longitude)
                mMap.addMarker(MarkerOptions().position(ubicacion).title("Marker in my actual position ${result.latitude} ${result.longitude}"))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicacion))
            }
        }

        // Add a marker in Sydney and move the camera
        //val sydney = LatLng(-34.0, 151.0)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun enableLocation(){
        if(!::mMap.isInitialized) return
        if(isLocationPermissionGranted()){
            mMap.isMyLocationEnabled = true
        }
        else {
            Toast.makeText(this, "Para activar ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        }
    }


    private fun requestLocationPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(this, "Entra a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),REQUEST_CODE_LOCATION)
        }
    }


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                mMap.isMyLocationEnabled = true
            } else {
                Toast.makeText(this, "Para activar ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
            }
            // Si acepta otro permiso
            else -> {
                Log.i(TAG.toString(), "Se han aceptado otros permisos")
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onResumeFragments() {
        super.onResumeFragments()
        if(!::mMap.isInitialized) return
        if(!isLocationPermissionGranted()){
            mMap.isMyLocationEnabled = false
            Toast.makeText(this, "Para activar ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        }
    }
}