package com.example.proyecto_alarma_pm

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Pantalla de bienvenida que se muestra al abrir la aplicación.
 */
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Habilita el diseño de borde a borde
        
        // Muestra el layout definido en activity_main.xml como pantalla de carga
        setContentView(R.layout.activity_main)

        // Handler para ejecutar una acción tras un retraso (delay)
        Handler(Looper.getMainLooper()).postDelayed({
            // Crea un Intent para saltar de la Splash a la lista principal (PillList)
            val intent = Intent(this, PillList::class.java)
            startActivity(intent)
            // Cerramos la Splash para que no se pueda volver atrás con el botón del móvil
            finish()
            }, 3000) // 3000 milisegundos = 3 segundos de espera


        // Ajuste de los márgenes para las barras del sistema (estado y navegación)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
