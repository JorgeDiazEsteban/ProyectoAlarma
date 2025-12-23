package com.example.proyecto_alarma_pm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
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

    // Launcher para recibir el resultado de AddPill (ahora recibe una lista)
    private val getPillResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val nuevas = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getSerializableExtra("Pills_List", ArrayList::class.java) as? ArrayList<Pill>
            } else {
                @Suppress("DEPRECATION")
                data?.getSerializableExtra("Pills_List") as? ArrayList<Pill>
            }

            nuevas?.let {
                // Añadimos todas las nuevas pastillas a nuestra lista principal
                myPills.addAll(it)
                adapter.notifyDataSetChanged()
                checkEmptyList()
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

        // Comprobamos si la lista está vacía al inicio
        checkEmptyList()

        // Botón para ir a añadir una pastilla
        binding.AddButton.setOnClickListener {
            val intent = Intent(this, AddPill::class.java)
            getPillResult.launch(intent)
        }

        binding.About.setOnClickListener {
            val intent = Intent(this, AboutUs::class.java)
            startActivity(intent)
        }
    }

    // Función para mostrar/ocultar el mensaje de "No hay medicamentos"
    private fun checkEmptyList() {
        if (myPills.isEmpty()) {
            binding.emptyStateCard.visibility = View.VISIBLE
            binding.pillsRecyclerView.visibility = View.GONE
        } else {
            binding.emptyStateCard.visibility = View.GONE
            binding.pillsRecyclerView.visibility = View.VISIBLE
        }
    }
}
