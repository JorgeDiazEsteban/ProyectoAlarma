package com.example.proyecto_alarma_pm

import android.content.Context
import java.io.*

/**
 * Objeto encargado de guardar y cargar la lista de medicamentos en el almacenamiento interno del dispositivo.
 * Utiliza serialización de objetos para guardar el ArrayList de forma binaria.
 */
object DataManager {
    private const val FILE_NAME = "pills_data.bin" // Nombre del archivo donde se guardarán los datos

    /**
     * Guarda la lista de pastillas en un archivo privado de la aplicación.
     */
    fun savePills(context: Context, pills: ArrayList<Pill>) {
        try {
            // Abrimos un flujo de salida hacia el archivo interno
            val fileOutputStream = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)
            // Convertimos el objeto ArrayList en un flujo de datos binarios
            val objectOutputStream = ObjectOutputStream(fileOutputStream)
            objectOutputStream.writeObject(pills)
            objectOutputStream.close()
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace() // Muestra errores en el log si algo falla al guardar
        }
    }

    /**
     * Carga la lista de pastillas desde el archivo interno.
     * Si el archivo no existe, devuelve una lista vacía.
     */
    @Suppress("UNCHECKED_CAST")
    fun loadPills(context: Context): ArrayList<Pill> {
        val pills = ArrayList<Pill>()
        try {
            // Intentamos abrir el archivo guardado
            val fileInputStream = context.openFileInput(FILE_NAME)
            val objectInputStream = ObjectInputStream(fileInputStream)
            // Leemos el objeto y lo convertimos de nuevo a ArrayList
            val obj = objectInputStream.readObject()
            if (obj is ArrayList<*>) {
                pills.addAll(obj as ArrayList<Pill>)
            }
            objectInputStream.close()
            fileInputStream.close()
        } catch (e: FileNotFoundException) {
            // Es normal que falle la primera vez que se usa la app (el archivo aún no existe)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return pills
    }
}
