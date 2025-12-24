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

/**
 * Receptor de alarmas que se activa cuando el sistema Android dispara una alarma programada.
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Recuperamos la lista de medicamentos para pasarla a la pantalla de la alarma
        val pills = intent.getSerializableExtra("Pills_List")
        
        // Creamos el Intent para abrir la pantalla de Alarma (la que suena y escanea)
        val alarmIntent = Intent(context, Alarm::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)      // Necesario para abrir actividad desde un Receiver
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)    // Evita abrir varias veces la misma pantalla
            putExtra("Pills_List", pills)                // Enviamos los datos para la validación
        }
        context.startActivity(alarmIntent)
    }
}

class AddPill : AppCompatActivity() {
    private lateinit var binding: ActivityAddPillBinding
    private var scannedText: String = ""
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var hoursList = ArrayList<String>()     // Horas añadidas para esta pastilla
    private var currentPills = ArrayList<Pill>()   // Lista total de pastillas del sistema

    // Lanzadores para permisos y cámara
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) openCamera() else Toast.makeText(this, "Permiso necesario", Toast.LENGTH_SHORT).show()
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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

        // 1. Recibir la lista actual desde PillList para poder añadir o borrar sobre ella
        val listaRecibida = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("Current_Pills", ArrayList::class.java) as? ArrayList<Pill>
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("Current_Pills") as? ArrayList<Pill>
        }
        currentPills = listaRecibida ?: ArrayList()

        // Botón para volver atrás
        binding.PagPrincipal.setOnClickListener { finish() }

        // Botón para añadir una hora a la lista de tomas del medicamento
        binding.btnAddHour.setOnClickListener {
            val hora = binding.NumAlarms.text.toString().trim()
            if (validarFormato(hora)) {
                hoursList.add(hora)
                binding.NumAlarms.text.clear()
                Toast.makeText(this, "Hora $hora añadida", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Eliminar: busca por nombre y borra el medicamento de la lista general
        binding.DeleteButton.setOnClickListener {
            val nameToDelete = binding.Name.text.toString().trim()
            val pillToRemove = currentPills.find { it.name.equals(nameToDelete, ignoreCase = true) }
            if (pillToRemove != null) {
                currentPills.remove(pillToRemove)
                enviarResultado() // Notifica a la pantalla principal el cambio
                binding.Name.text.clear()
                Toast.makeText(this, "Eliminado: $nameToDelete", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No encontrado", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Guardar: Crea el objeto Pill y programa las alarmas en el sistema
        binding.SaveButton.setOnClickListener {
            val name = binding.Name.text.toString().trim()
            val duration = binding.Duration.text.toString().trim()
            if (name.isBlank() || hoursList.isEmpty() || duration.isBlank()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newPill = Pill(name, hoursList.size, hoursList, duration)
            currentPills.add(newPill)

            programarTodasLasAlarmas() // Registra las horas en el AlarmManager de Android
            enviarResultado()
            finish()
        }

        binding.btnScan.setOnClickListener { checkCameraPermission() }
    }

    // Informa a PillList.kt de que la lista de medicamentos ha cambiado
    private fun enviarResultado() {
        val intent = Intent()
        intent.putExtra("Pills_List", currentPills)
        setResult(RESULT_OK, intent)
    }

    // Comprueba el formato de hora HH:MM mediante una expresión regular
    private fun validarFormato(texto: String): Boolean {
        val patron = Regex("^([01][0-9]|2[0-3]):[0-5][0-9]$")
        return patron.matches(texto)
    }

    // Configura el AlarmManager para que Android active el AlarmReceiver a las horas dadas
    private fun programarTodasLasAlarmas() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestData = System.currentTimeMillis().toInt()

        hoursList.forEachIndexed { indice, textoHora ->
            try {
                val partes = textoHora.split(":")
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, partes[0].toInt())
                    set(Calendar.MINUTE, partes[1].toInt())
                    set(Calendar.SECOND, 0)
                    if (before(Calendar.getInstance())) add(Calendar.DATE, 1) // Si ya pasó, para mañana
                }

                val intent = Intent(this, AlarmReceiver::class.java).apply {
                    putExtra("Pills_List", currentPills)
                }
                
                val pendingIntent = PendingIntent.getBroadcast(this, requestData + indice, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                
                // setExactAndAllowWhileIdle asegura que suene aunque el móvil esté en ahorro de batería
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // Procesa la foto de la cámara para detectar texto (Nombre del medicamento)
    private fun processImageWithLens(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image).addOnSuccessListener { visionText ->
            if (visionText.text.isNotBlank()) binding.Name.setText(visionText.text.trim())
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) openCamera()
        else requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }
}
