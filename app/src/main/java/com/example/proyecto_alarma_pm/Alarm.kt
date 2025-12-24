package com.example.proyecto_alarma_pm

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.WindowManager
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

/**
 * Pantalla que aparece cuando se dispara una alarma.
 * Incluye la música de alerta y el sistema de escaneo para detenerla.
 */
class Alarm : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmBinding
    private var mediaPlayer: MediaPlayer? = null
    private var pillList = ArrayList<Pill>()
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Lanza la cámara para tomar la foto del medicamento
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let { processImage(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // CONFIGURACIÓN PARA DESPERTAR PANTALLA: Permite que la alarma se vea sobre el bloqueo y encienda la pantalla
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }

        enableEdgeToEdge()
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ajuste de márgenes para diseño de pantalla completa
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- 1. Audio ---
        try {
            // Carga e inicia el sonido de alarma en bucle (res/raw/alarma_sonido.mp3)
            mediaPlayer = MediaPlayer.create(this, R.raw.alarma_sonido)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al reproducir sonido", Toast.LENGTH_SHORT).show()
        }

        // --- 2. Datos ---
        // Recibe la lista de medicamentos válidos para esta alarma
        val lista = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("Pills_List", ArrayList::class.java) as? ArrayList<Pill>
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("Pills_List") as? ArrayList<Pill>
        }
        pillList = lista ?: ArrayList()

        // --- 3. Acción ---
        // Al pulsar el botón, el usuario debe escanear el bote físico
        binding.btnStopAlarm.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(cameraIntent)
        }
    }

    // Procesa la imagen capturada con Google ML Kit (Reconocimiento de Texto)
    private fun processImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image).addOnSuccessListener { visionText ->
            val scannedName = visionText.text.trim()
            
            // Busca si el nombre escaneado coincide con alguno de la lista (ignorando mayúsculas/minúsculas)
            val match = pillList.any { it.name.equals(scannedName, ignoreCase = true) }

            if (match) {
                // Si el nombre es correcto, se apaga la música y se cierra la pantalla
                stopAlarm()
                Toast.makeText(this, "¡Correcto! Medicina detectada.", Toast.LENGTH_LONG).show()
                finish()
            } else {
                // Si no coincide, la alarma sigue sonando hasta que escanee el bote correcto
                Toast.makeText(this, "El nombre '$scannedName' no es correcto.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para liberar recursos del reproductor de audio
    private fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm() // Asegura que el sonido para si se cierra la actividad
    }
}
