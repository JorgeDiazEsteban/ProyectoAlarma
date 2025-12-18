package com.example.proyecto_alarma_pm

import java.io.Serializable

data class Pill(
    val Name: String,
    val NumAlarms: Int,
    val Hours: ArrayList<String>,
    val Duration: String
) : Serializable
