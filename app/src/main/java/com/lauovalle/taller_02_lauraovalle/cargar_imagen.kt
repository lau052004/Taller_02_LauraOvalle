package com.lauovalle.taller_02_lauraovalle

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.snackbar.Snackbar
import com.lauovalle.taller_02_lauraovalle.databinding.ActivityCargarImagenBinding
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.util.Date
import java.util.logging.Logger


class cargar_imagen : AppCompatActivity() {
    //--------------------------------------------------------
    private lateinit var binding: ActivityCargarImagenBinding
    private val REQUEST_VIDEO_CAPTURE = 1
    //--------------------------------------------------------
    companion object {
        val TAG: String = cargar_imagen::class.java.name
    }

    private val logger = Logger.getLogger(TAG)

    // Permission handler
    private val getSimplePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) {
        updateUI(it)
    }

    var pictureImagePath: Uri? = null
    //var imageViewContainer: ImageView? = null

    // Create ActivityResultLauncher instances
    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Handle camera result
            binding.imageView!!.setImageURI(pictureImagePath)
            binding.imageView!!.scaleType = ImageView.ScaleType.FIT_CENTER
            binding.imageView!!.adjustViewBounds = true
            logger.info("Image capture successfully.")
        } else {
            logger.warning("Image capture failed.")
        }
    }

    //--------------------------------------------------------
    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {uri ->
        if(uri!=null){
            // Imagen seleccionada
            binding.imageView.setImageURI(uri)
        } else {
            // No imagen
            Log.INFO
        }
    }
    //--------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding.imageView.visibility = View.INVISIBLE
        //binding.videoView.visibility = View.INVISIBLE
        // --------------------  BINDING
        binding = ActivityCargarImagenBinding.inflate(layoutInflater)
        // llama al método getRoot() para obtener una referencia a la vista raíz
        val view: View = binding.root
        setContentView(view)

        // Pick Image from gallery
        binding.botonPick.setOnClickListener{
            binding.imageView.visibility = View.VISIBLE
            binding.videoView.visibility = View.INVISIBLE
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // Take photo or video
        binding.botonTake.setOnClickListener {
                // Pedir el permiso cuando la aplicación inicie
                logger.info("Se va a solicitar el permiso")
                verifyPermissions(this, android.Manifest.permission.CAMERA, "El permiso es requerido para...")
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
    fun updateUI(permission : Boolean) {
        if(permission){
            //granted
            logger.info("Permission granted")
            if(binding.switchToggle.isChecked)
            {
                binding.imageView.visibility = View.INVISIBLE
                binding.videoView.visibility = View.VISIBLE
                dispatchTakeVideoIntent()
            }
            else {
                binding.imageView.visibility = View.VISIBLE
                binding.videoView.visibility = View.INVISIBLE
                dipatchTakePictureIntent()
            }
        }else{
            logger.warning("Permission denied")
        }
    }


    fun dipatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Crear el archivo donde debería ir la foto
        var imageFile: File? = null
        try {
            imageFile = createImageFile()
        } catch (ex: IOException) {
            logger.warning(ex.message)
        }
        // Continua si el archivo ha sido creado exitosamente
        if (imageFile != null) {
            // Guardar un archivo: Ruta para usar con ACTION_VIEW intents
            pictureImagePath = FileProvider.getUriForFile(this,"com.example.android.fileprovider", imageFile)
            logger.info("Ruta: ${pictureImagePath}")
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureImagePath)
            try {
                cameraActivityResultLauncher.launch(takePictureIntent)
            } catch (e: ActivityNotFoundException) {
                logger.warning("Camera app not found.")
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        //Crear un nombre de archivo de imagen
        val timeStamp: String = DateFormat.getDateInstance().format(Date())
        val imageFileName = "${timeStamp}.jpg"
        val imageFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),imageFileName)
        return imageFile
    }

    private fun dispatchTakeVideoIntent() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_VIDEO_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            val videoUri: Uri? = data?.data
            binding.videoView.setVideoURI(videoUri)
            binding.videoView.start()
        }
    }
}