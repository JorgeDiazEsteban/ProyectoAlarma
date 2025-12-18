package com.example.proyecto_alarma_pm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_alarma_pm.databinding.ActivityAddPillBinding

class AddPill : AppCompatActivity() {
    private lateinit var binding: ActivityAddPillBinding

    private var NameTaked: String = " "
    private var HoursList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddPillBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.OpenCamera.setOnClickListener {
            //camera logic
        }
        binding.SaveButton.setOnClickListener {
            val new = Pill(
                binding.Name.text.toString(),
                binding.NumAlarms.text.toString().toInt(),
                HoursList,
                binding.Duration.text.toString()
            )
            val intent = Intent()
            intent.putExtra("New_Pill",new)
            setResult(RESULT_OK,intent)
            finish()
        }












        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}