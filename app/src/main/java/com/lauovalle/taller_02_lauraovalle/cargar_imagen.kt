package com.lauovalle.taller_02_lauraovalle

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.lauovalle.taller_02_lauraovalle.databinding.ActivityCargarImagenBinding


class cargar_imagen : AppCompatActivity() {
    private lateinit var binding: ActivityCargarImagenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --------------------  BINDING
        binding = ActivityCargarImagenBinding.inflate(layoutInflater)
        // llama al método getRoot() para obtener una referencia a la vista raíz
        val view: View = binding.root
        setContentView(view)

        binding.botonTake.setOnClickListener{

        }

    }
}