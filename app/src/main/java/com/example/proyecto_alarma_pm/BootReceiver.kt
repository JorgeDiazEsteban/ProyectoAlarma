package com.example.proyecto_alarma_pm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BOOT", "Móvil encendido. Reprogramando alarmas...")
            
            // Cargamos la lista guardada en disco
            val pills = DataManager.loadPills(context)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            pills.forEach { pill ->
                pill.hours.forEachIndexed { index, time ->
                    try {
                        val parts = time.split(":")
                        val hour = parts[0].toInt()
                        val minute = parts[1].toInt()

                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                            if (before(Calendar.getInstance())) {
                                add(Calendar.DATE, 1)
                            }
                        }

                        val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                            putExtra("Pills_List", pills)
                        }

                        // Generamos un ID único para cada alarma
                        val requestId = (pill.name.hashCode() + index + calendar.timeInMillis.toInt())
                        
                        val pendingIntent = PendingIntent.getBroadcast(
                            context, requestId, alarmIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                pendingIntent
                            )
                        } else {
                            alarmManager.setExact(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                pendingIntent
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
