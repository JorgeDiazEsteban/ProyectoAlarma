package com.example.proyecto_alarma_pm

import java.io.Serializable

data class Pill(
    val name: String,
    val numAlarms: Int,
    val hours: ArrayList<String>,
    val duration: String
) : Serializable
