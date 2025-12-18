package com.example.proyecto_alarma_pm

import android.Manifest
import androidx.core.content.ContextCompat

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_alarma_pm.databinding.ActivityAddPillBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class AddPill : AppCompatActivity() {
    private lateinit var binding: ActivityAddPillBinding
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var NameTaked: String = " "
    private var HoursList = ArrayList<String>()

    // 1. Los lanzadores (Launchers) DEBEN estar aquí, a nivel de clase
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Permiso de cámara necesario para escanear", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let { processImageWithLens(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddPillBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón de guardar
        binding.SaveButton.setOnClickListener {
            val new = Pill(
                binding.Name.text.toString(),
                binding.NumAlarms.text.toString().toInt(),
                HoursList,
                binding.Duration.text.toString()
            )
            val intent = Intent()
            intent.putExtra("New_Pill", new)
            setResult(RESULT_OK, intent)
            finish()
        }

        // Botón de escanear (referencia correcta al ID de tu XML)
        binding.btnScan.setOnClickListener {
            checkCameraPermission()
        }
    }

    // --- FUNCIONES MOVIDAS FUERA DE ONCREATE ---

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun processImageWithLens(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                if (visionText.text.isNotBlank()) {
                    // Asegúrate de que este ID existe en tu activity_add_pill.xml
                    binding.tvResult.text = visionText.text
                } else {
                    binding.tvResult.text = "No se detectó texto."
                }
            }
            .addOnFailureListener { e ->
                binding.tvResult.text = "Error: ${e.message}"
            }
    }
}
