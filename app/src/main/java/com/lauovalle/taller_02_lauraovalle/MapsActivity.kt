package com.lauovalle.taller_02_lauraovalle

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.lauovalle.taller_02_lauraovalle.databinding.ActivityMapsBinding
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.logging.Logger

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        val REQUEST_CODE_LOCATION = 0
        const val lowerLeftLatitude = 40.65129083311869
        const val lowerLeftLongitude = -74.08836562217077
        const val upperRightLatitude = 40.89464984144272
        const val upperRightLongitude = -73.82023056066367
        val TAG: String = MapsActivity::class.java.name
    }

    // GLOBALES ----------------------------------------------------------------
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mMap: GoogleMap
    private val locationService:LocationService = LocationService()
    lateinit var sensorManager: SensorManager
    lateinit var lightSensor: Sensor
    lateinit var lightSensorListener: SensorEventListener
    private lateinit var mGeocoder: Geocoder
    lateinit var mAddress: EditText
    // Añade una variable para controlar si el mapa está listo
    private var isMapReady = false
    private var findingAdress = false


    private val logger = Logger.getLogger(TAG)

    // Permission handler
    private val getSimplePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) {
        updateUI(it)
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

        mAddress = binding.address
        mAddress.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                findingAdress = true
                findAddress()
            }
            false
        }

        // Initialize the sensors
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!

        // Initialize the listener
        lightSensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (mMap != null) {
                    if (event.values[0] < 5000) {
                        Log.i("MAPS", "DARK MAP " + event.values[0])
                        mMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                this@MapsActivity,
                                R.raw.style_night
                            )
                        )
                    } else {
                        Log.i("MAPS", "LIGHT MAP " + event.values[0])
                        mMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                this@MapsActivity,
                                R.raw.style_day_retro
                            )
                        )
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
        }
        // Initialize the geocoder
        mGeocoder = Geocoder(baseContext)

    }

    override fun onResume() {
        super.onResume()
        if(isMapReady)
        {
            sensorManager.registerListener(
                lightSensorListener,
                lightSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onPause() {
        super.onPause()
        if(isMapReady)
        {
            sensorManager.unregisterListener(lightSensorListener)
        }
    }

    // ON_CREATE ----------------------------------------------------------------
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        isMapReady = true
        mMap.moveCamera(CameraUpdateFactory.zoomTo(20f))
        mMap.uiSettings.setAllGesturesEnabled(true)
        /// Add UI controls
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        verifyPermissions(this, android.Manifest.permission.ACCESS_FINE_LOCATION, "El permiso es requerido para poder mostrar tu ubicación en el mapa")
    }

    fun search(view: View?) {
        mMap.clear()
        findAddress()
    }

    private fun findAddress() {
        val addressString = mAddress.text.toString()
        if (addressString.isNotEmpty()) {
            try {
                val addresses = mGeocoder.getFromLocationName(
                    addressString, 3
                )
                if (addresses != null && !addresses.isEmpty()) {
                    val addressResult = addresses[0]
                    val position = LatLng(addressResult.latitude, addressResult.longitude)
                    mMap.addMarker(
                        MarkerOptions().position(position)
                            .title(addressResult.featureName)
                            .snippet(addressResult.getAddressLine(0))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    )
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(position))
                } else {
                    Toast.makeText(
                        this@MapsActivity,
                        "Dirección no encontrada",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this@MapsActivity, "La dirección está vacía", Toast.LENGTH_SHORT)
                .show()
        }
    }


    private fun verifyPermissions(context: Context, permission: String, rationale: String) {
        when {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                Snackbar.make(binding.root, "Ya tengo los permisos 😜", Snackbar.LENGTH_LONG).show()
                updateUI(true)
            }
            shouldShowRequestPermissionRationale(permission) -> {
                // We display a snackbar with the justification for the permission, and once it disappears, we request it again.
                val snackbar = Snackbar.make(binding.root, rationale, Snackbar.LENGTH_LONG)
                snackbar.addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(snackbar: Snackbar, event: Int) {
                        if (event == DISMISS_EVENT_TIMEOUT) {
                            getSimplePermission.launch(permission)
                        }
                    }
                })
                snackbar.show()
            }
            else -> {
                getSimplePermission.launch(permission)
            }
        }
    }

    // Update activity behavior and actions according to result of permission request
    @SuppressLint("MissingPermission")
    fun updateUI(permission : Boolean) {
        if (permission) {
            //granted
            logger.info("Permission granted")
            mMap.isMyLocationEnabled = true

            if(findingAdress==false){
                lifecycleScope.launch {
                    val result = locationService.getUserLocation(this@MapsActivity)
                    if(result!=null)
                    {
                        val ubicacion = LatLng(result.latitude, result.longitude)
                        mMap.addMarker(MarkerOptions().position(ubicacion).title("Marker in my actual position ${result.latitude} ${result.longitude}"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicacion))
                    }
                    locationService.getRoute(this@MapsActivity,mMap)
                }
            }
        } else {
            logger.warning("Permission denied")
            mMap.isMyLocationEnabled = false
        }
    }
}