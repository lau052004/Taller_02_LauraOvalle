package com.lauovalle.taller_02_lauraovalle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.lauovalle.taller_02_lauraovalle.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --------------------  BINDING
        binding = ActivityMainBinding.inflate(layoutInflater)
        // llama al método getRoot() para obtener una referencia a la vista raíz
        val view: View = binding.root
        setContentView(view)


        // --------------------  INTENT
        binding.imagenCamara.setOnClickListener{
            val miIntent = Intent(this, cargar_imagen::class.java)
            startActivity(miIntent)
        }

    }
}