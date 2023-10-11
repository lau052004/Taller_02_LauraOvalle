package com.lauovalle.taller_02_lauraovalle

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.lauovalle.taller_02_lauraovalle.databinding.ActivityMapsBinding
import java.util.logging.Logger

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    // GLOBALES ----------------------------------------------------------------
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mMap: GoogleMap
    companion object {
        val TAG: String = cargar_imagen::class.java.name
    }

    private val logger = Logger.getLogger(TAG)

    // Permission handler
    @SuppressLint("MissingPermission")
    private val getSimplePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) {
        if(it)
        {
            logger.info("Permission granted")
            mMap.isMyLocationEnabled = true
        }
        else
        {
            logger.info("Permission dennied")
        }
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

        // Pedir el permiso cuando la aplicación inicie
        logger.info("Se va a solicitar el permiso")
        verifyPermissions(this, android.Manifest.permission.ACCESS_FINE_LOCATION, "El permiso es requerido para...")

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun verifyPermissions(context: Context, permission: String, rationale: String) {
        when {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                Snackbar.make(binding.root, "Ya tengo los permisos 😜", Snackbar.LENGTH_LONG).show()
                logger.info("Permission granted")
                mMap.isMyLocationEnabled = true
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
}