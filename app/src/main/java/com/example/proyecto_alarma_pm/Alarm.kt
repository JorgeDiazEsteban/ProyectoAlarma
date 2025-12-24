package com.example.proyecto_alarma_pm

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_alarma_pm.databinding.ActivityAlarmBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class Alarm : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmBinding
    private var mediaPlayer: MediaPlayer? = null
    private var pillList = ArrayList<Pill>()
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let { processImage(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Configuración de ViewBinding
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Iniciar música en bucle
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.alarma_sonido)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al reproducir sonido", Toast.LENGTH_SHORT).show()
        }

        // 2. Recibir lista de medicamentos
        val lista = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("Pills_List", ArrayList::class.java) as? ArrayList<Pill>
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("Pills_List") as? ArrayList<Pill>
        }
        pillList = lista ?: ArrayList()

        // 3. Botón para escanear usando Binding
        binding.btnStopAlarm.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(cameraIntent)
        }
    }

    private fun processImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val scannedName = visionText.text.trim()
                
                // Comprobar si coincide con alguno de la lista
                val match = pillList.any { it.name.equals(scannedName, ignoreCase = true) }

                if (match) {
                    stopAlarm()
                    Toast.makeText(this, "¡Correcto! Medicina detectada.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "El nombre '$scannedName' no es correcto.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
}
