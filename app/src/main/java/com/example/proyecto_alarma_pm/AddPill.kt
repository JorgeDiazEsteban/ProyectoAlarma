package com.example.proyecto_alarma_pm

import android.Manifest // Importante para el permiso
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.proyecto_alarma_pm.databinding.ActivityAddPillBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class AddPill : AppCompatActivity() {

    private lateinit var binding: ActivityAddPillBinding

    private var scannedText: String = ""
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // 1. Lanzador para solicitar permiso de cámara
    private var HoursList = ArrayList<String>()

    // Lista de pastillas actuales recibidas desde la lista principal
    private var currentPills = ArrayList<Pill>()
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Permiso de cámara necesario para escanear", Toast.LENGTH_SHORT)
                .show()
        }
    }

    // 2. Lanzador para capturar la foto (Ya lo tenías, mantenlo)
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                imageBitmap?.let { processImageWithLens(it) }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPillBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Recibir la lista actual de medicamentos de forma segura
        val listaRecibida = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("Current_Pills", ArrayList::class.java) as? ArrayList<Pill>
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("Current_Pills") as? ArrayList<Pill>
        }
        currentPills = listaRecibida ?: ArrayList()

        binding.PagPrincipal.setOnClickListener {
            finish()
        }

        // Botón Guardar
        binding.SaveButton.setOnClickListener {
            val name = binding.Name.text.toString().trim()
            val numAlarmsStr = binding.NumAlarms.text.toString()
            val duration = binding.Duration.text.toString()

            if (name.isBlank() || numAlarmsStr.isBlank() || duration.isBlank()) {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val newPill = Pill(name, numAlarmsStr.toIntOrNull() ?: 0, HoursList, duration)
            currentPills.add(newPill)

            enviarResultado()

            binding.Name.text.clear()
            binding.NumAlarms.text.clear()
            binding.Duration.text.clear()
            Toast.makeText(this, "Añadida: $name", Toast.LENGTH_SHORT).show()
        }

        // 2. Lógica del Botón Eliminar corregida
        binding.DeleteButton.setOnClickListener {
            val nameToDelete = binding.Name.text.toString().trim()

            if (nameToDelete.isBlank()) {
                Toast.makeText(
                    this,
                    "Escanea o escribe un nombre para eliminar",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Buscamos la pastilla en la lista que recibimos (currentPills)
            val pillToRemove = currentPills.find { it.name.equals(nameToDelete, ignoreCase = true) }

            if (pillToRemove != null) {
                currentPills.remove(pillToRemove)
                enviarResultado()

                binding.Name.text.clear()
                Toast.makeText(this, "Eliminado: $nameToDelete", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "El medicamento '$nameToDelete' no existe", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.btnScan.setOnClickListener {
            checkCameraPermission()
        }
    }

    // 3. Función para verificar si ya tenemos el permiso
    // Función auxiliar para enviar siempre la lista actualizada de vuelta
    private fun enviarResultado() {
        val intent = Intent()
        intent.putExtra("Pills_List", currentPills)
        setResult(RESULT_OK, intent)
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
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

    private fun processImageWithLens(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                if (visionText.text.isNotBlank()) {
                    val detectedText = visionText.text
                    if (detectedText.isNotBlank()) {
                        scannedText = detectedText.trim()
                        binding.Name.setText(scannedText)
                        Toast.makeText(this, "Nombre detectado: $scannedText", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, "No se detectó texto.", Toast.LENGTH_SHORT).show()
                    }
                }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
    }
