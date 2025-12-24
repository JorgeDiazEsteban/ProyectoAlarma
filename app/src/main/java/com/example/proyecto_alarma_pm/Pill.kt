package com.example.proyecto_alarma_pm

import java.io.Serializable

/**
 * Clase de datos que representa un Medicamento (Pastilla).
 * Implementa Serializable para que los objetos de esta clase puedan enviarse
 * entre diferentes pantallas (Activities) a través de Intent.
 */
data class Pill(
    val name: String,           // Nombre del medicamento (ej: Ibuprofeno)
    val numAlarms: Int,        // Cantidad de veces que suena al día
    val hours: ArrayList<String>, // Lista de horas programadas (ej: ["08:00", "20:00"])
    val duration: String       // Duración del tratamiento (ej: 7 días)
) : Serializable
