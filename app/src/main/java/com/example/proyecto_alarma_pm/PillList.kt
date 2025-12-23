package com.example.proyecto_alarma_pm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_alarma_pm.databinding.ActivityPillListBinding
import java.io.Serializable
data class Pill(
    val Name: String,
    val NumAlarms: Int,
    val Hours: ArrayList<String>,
    val Duration: String
) : Serializable
class PillList : AppCompatActivity() {
    private lateinit var binding: ActivityPillListBinding
//creation Pill List were the Pills are gonna get saved
    private val MyPills = ArrayList<Pill>()
    val getPillResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data!!
            val nueva = data.getSerializableExtra("New_Pill") as Pill

            MyPills.add(nueva)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityPillListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.AddButton.setOnClickListener {
            val intent = Intent(this, AddPill::class.java)
            getPillResult.launch(intent)
        }

        binding.Camera.setOnClickListener {
            //no se como s abre la camara aun
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets



        }
    }
}