package com.example.proyecto_alarma_pm

import android.content.Context
import java.io.*

object DataManager {
    private const val FILE_NAME = "pills_data.bin"

    fun savePills(context: Context, pills: ArrayList<Pill>) {
        try {
            val fileOutputStream = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)
            val objectOutputStream = ObjectOutputStream(fileOutputStream)
            objectOutputStream.writeObject(pills)
            objectOutputStream.close()
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun loadPills(context: Context): ArrayList<Pill> {
        val pills = ArrayList<Pill>()
        try {
            val fileInputStream = context.openFileInput(FILE_NAME)
            val objectInputStream = ObjectInputStream(fileInputStream)
            val obj = objectInputStream.readObject()
            if (obj is ArrayList<*>) {
                pills.addAll(obj as ArrayList<Pill>)
            }
            objectInputStream.close()
            fileInputStream.close()
        } catch (e: FileNotFoundException) {
            // Es la primera vez que se abre la app
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return pills
    }
}
