package com.example.proyecto_alarma_pm


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_alarma_pm.databinding.ActivityAddPillBinding
import android.Manifest // Importante para el permiso
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class AddPill : AppCompatActivity() {
    private lateinit var binding: ActivityAddPillBinding

    private var NameTaked: String = " "
    private var HoursList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddPillBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.SaveButton.setOnClickListener {
            val new = Pill(
                binding.Name.text.toString(),
                binding.NumAlarms.text.toString().toInt(),
                HoursList,
                binding.Duration.text.toString()
            )
            val intent = Intent()
            intent.putExtra("New_Pill",new)
            setResult(RESULT_OK,intent)
            finish()
        }
         val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        // 1. Lanzador para solicitar permiso de cámara
         val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, "Permiso de cámara necesario para escanear", Toast.LENGTH_SHORT).show()

            }
        }

        // 2. Lanzador para capturar la foto (Ya lo tenías, mantenlo)
         val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                imageBitmap?.let { processImageWithLens(it) }
            }
        }

         override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityAddPillBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.btnScan.setOnClickListener {
                checkCameraPermission()
            }
        }

        // 3. Función para verificar si ya tenemos el permiso
         fun checkCameraPermission() {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                    openCamera()
                }
                else -> {
                    // Solicita el permiso directamente
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }

         private fun openCamera() {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        }

         fun processImageWithLens(bitmap: Bitmap) {
            val image = InputImage.fromBitmap(bitmap, 0)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    if (visionText.text.isNotBlank()) {
                        binding.tvResult.text = visionText.text
                    } else {
                        binding.tvResult.text = "No se detectó texto."
                    }
                }
                .addOnFailureListener { e ->
                    binding.tvResult.text = "Error: ${e.message}"
                }
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets



        }
    }
}