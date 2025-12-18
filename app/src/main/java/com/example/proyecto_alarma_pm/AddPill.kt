package com.example.proyecto_alarma_pm

import android.Manifest
import android.app.Activity
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
    private var scannedText: String = ""
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var HoursList = ArrayList<String>()

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

        // Botón Cámara (Cambiado para que abra la cámara en lugar de abrir PillList otra vez)
        binding.PagPrincipal.setOnClickListener {
            val intent = Intent(this, PillList::class.java)
            startActivity(intent)
        }

        // Botón de guardar
        binding.SaveButton.setOnClickListener {
            val name = binding.Name.text.toString()
            val numAlarmsStr = binding.NumAlarms.text.toString()
            val duration = binding.Duration.text.toString()

            if (name.isBlank() || numAlarmsStr.isBlank() || duration.isBlank()) {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val numAlarms = numAlarmsStr.toIntOrNull() ?: 0
            
            val newPill = Pill(
                name,
                numAlarms,
                HoursList,
                duration
            )

            val intent = Intent()
            intent.putExtra("New_Pill", newPill)
            setResult(RESULT_OK, intent)
            
            // Limpiamos los campos por si acaso, aunque al hacer finish() ya no se verán
            binding.Name.text.clear()
            binding.NumAlarms.text.clear()
            binding.Duration.text.clear()
            
            Toast.makeText(this, "Pastilla guardada", Toast.LENGTH_SHORT).show()
            
        }

        // Botón de escanear (referencia al ID del XML)
        binding.btnScan.setOnClickListener {
            checkCameraPermission()
        }
    }

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
                    scannedText = visionText.text
                    binding.tvResult.text = scannedText
                    // Ponemos el texto detectado en el campo de Nombre automáticamente
                    binding.Name.setText(scannedText)
                } else {
                    binding.tvResult.text = "No se detectó texto."
                }
            }
            .addOnFailureListener { e ->
                binding.tvResult.text = "Error: ${e.message}"
            }
    }
}
