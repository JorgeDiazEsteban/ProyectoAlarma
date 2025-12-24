package com.example.proyecto_alarma_pm

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import androidx.core.content.ContextCompat
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_alarma_pm.databinding.ActivityAddPillBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

// El Receiver DEBE estar fuera o ser estático para que el Manifest lo encuentre
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Para que se vea aunque la app esté cerrada
        Toast.makeText(context, "¡ATENCIÓN! Hora de tu medicina", Toast.LENGTH_LONG).show()
        Log.d("ALARMAS", "¡Alarma recibida con éxito!")
    }
}

class AddPill : AppCompatActivity() {
    private lateinit var binding: ActivityAddPillBinding
    private var scannedText: String = ""
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var hoursList = ArrayList<String>()
    private var currentPills = ArrayList<Pill>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) openCamera()
        else Toast.makeText(this, "Permiso necesario", Toast.LENGTH_SHORT).show()
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

        // Recuperar lista actual
        val listaRecibida = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("Current_Pills", ArrayList::class.java) as? ArrayList<Pill>
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("Current_Pills") as? ArrayList<Pill>
        }
        currentPills = listaRecibida ?: ArrayList()

        binding.PagPrincipal.setOnClickListener { finish() }

        // Botón para añadir horas
        binding.btnAddHour.setOnClickListener {
            val hora = binding.NumAlarms.text.toString().trim()
            if (validarFormato(hora)) {
                hoursList.add(hora)
                binding.NumAlarms.text.clear()
                Toast.makeText(this, "Hora $hora añadida", Toast.LENGTH_SHORT).show()
            }
        }

        // REPARADO: Botón Eliminar
        binding.DeleteButton.setOnClickListener {
            val nameToDelete = binding.Name.text.toString().trim()
            if (nameToDelete.isBlank()) {
                Toast.makeText(this, "Escribe el nombre a eliminar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val pillToRemove = currentPills.find { it.name.equals(nameToDelete, ignoreCase = true) }
            if (pillToRemove != null) {
                currentPills.remove(pillToRemove)
                enviarResultado()
                binding.Name.text.clear()
                Toast.makeText(this, "Eliminado: $nameToDelete", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No se encontró: $nameToDelete", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Guardar
        binding.SaveButton.setOnClickListener {
            val name = binding.Name.text.toString().trim()
            val duration = binding.Duration.text.toString().trim()
            
            if (name.isBlank() || hoursList.isEmpty() || duration.isBlank()) {
                Toast.makeText(this, "Faltan datos o añadir horas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newPill = Pill(name, hoursList.size, hoursList, duration)
            currentPills.add(newPill)

            programarTodasLasAlarmas()
            enviarResultado()
            finish()
        }

        binding.btnScan.setOnClickListener { checkCameraPermission() }
    }

    private fun enviarResultado() {
        val intent = Intent()
        intent.putExtra("Pills_List", currentPills)
        setResult(RESULT_OK, intent)
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun validarFormato(texto: String): Boolean {
        val patron = Regex("^([01][0-9]|2[0-3]):[0-5][0-9]$")
        return patron.matches(texto)
    }

    private fun programarTodasLasAlarmas() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Usar la hora actual para generar IDs únicos de alarmas y no sobrescribirlas
        val requestData = System.currentTimeMillis().toInt()

        hoursList.forEachIndexed { indice, textoHora ->
            try {
                val partes = textoHora.split(":")
                val hora = partes[0].toInt()
                val minutos = partes[1].toInt()

                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hora)
                    set(Calendar.MINUTE, minutos)
                    set(Calendar.SECOND, 0)
                    // Si la hora ya pasó hoy, programarla para mañana
                    if (before(Calendar.getInstance())) {
                        add(Calendar.DATE, 1)
                    }
                }

                val intent = Intent(this, AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    this, requestData + indice, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Usamos setExactAndAllowWhileIdle para que funcione incluso en modo ahorro
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        cal.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        cal.timeInMillis,
                        pendingIntent
                    )
                }
                
                Log.d("ALARMAS", "Alarma programada para: ${cal.time}")
            } catch (e: Exception) {
                Log.e("ALARMAS", "Error: ${e.message}")
            }
        }
    }

    private fun processImageWithLens(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val detectedText = visionText.text
                if (detectedText.isNotBlank()) {
                    binding.Name.setText(detectedText.trim())
                }
            }
    }
}
