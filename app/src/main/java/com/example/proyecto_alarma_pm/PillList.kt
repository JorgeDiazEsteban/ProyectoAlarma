package com.example.proyecto_alarma_pm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_alarma_pm.databinding.ActivityPillListBinding

class PillList : AppCompatActivity() {
    private lateinit var binding: ActivityPillListBinding
    private val myPills = ArrayList<Pill>()
    private lateinit var adapter: PillAdapter

    // Launcher para recibir el resultado de AddPill
    private val getPillResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val nueva = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getSerializableExtra("New_Pill", Pill::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getSerializableExtra("New_Pill") as? Pill
            }

            nueva?.let {
                myPills.add(it)
                adapter.notifyItemInserted(myPills.size - 1)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPillListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar el Adaptador y el RecyclerView
        adapter = PillAdapter(myPills)
        binding.pillsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.pillsRecyclerView.adapter = adapter

        // Botón para ir a añadir una pastilla
        binding.AddButton.setOnClickListener {
            val intent = Intent(this, AddPill::class.java)
            getPillResult.launch(intent)
        }

        binding.Camera.setOnClickListener {
            // Acción opcional para "Ver Todo"
        }
    }
}
