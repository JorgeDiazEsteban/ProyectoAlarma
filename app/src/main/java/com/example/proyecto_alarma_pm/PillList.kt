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

/**
 * Actividad principal que muestra la lista de medicamentos programados.
 */
class PillList : AppCompatActivity() {
    private lateinit var binding: ActivityPillListBinding
    private val myPills = ArrayList<Pill>() // Lista local de medicamentos
    private lateinit var adapter: PillAdapter

    // Launcher para iniciar AddPill y recibir la lista actualizada de vuelta
    private val getPillResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            // Recibimos la lista serializada (compatible con diferentes versiones de Android)
            val nuevas = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getSerializableExtra("Pills_List", ArrayList::class.java) as? ArrayList<Pill>
            } else {
                data?.getSerializableExtra("Pills_List") as? ArrayList<Pill>
            }

            nuevas?.let {
                // Actualizamos la lista local y notificamos al adaptador para que refresque la vista
                myPills.clear()
                myPills.addAll(it)
                adapter.notifyDataSetChanged()
                checkEmptyList()
                
                // Guardamos los cambios en el almacenamiento persistente (disco)
                DataManager.savePills(this, myPills)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPillListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cargamos los medicamentos guardados previamente al abrir la app
        myPills.addAll(DataManager.loadPills(this))

        // Configuración de márgenes para barras de sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Si hubo una rotación de pantalla, recuperamos la lista del bundle temporal
        if (savedInstanceState != null) {
            val savedPills = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                savedInstanceState.getSerializable("saved_pills", ArrayList::class.java) as? ArrayList<Pill>
            } else {
                @Suppress("DEPRECATION")
                savedInstanceState.getSerializable("saved_pills") as? ArrayList<Pill>
            }

            savedPills?.let {
                myPills.clear()
                myPills.addAll(it)
            }
        }

        // Configuramos el RecyclerView con su adaptador y administrador de diseño
        adapter = PillAdapter(myPills)
        binding.pillsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.pillsRecyclerView.adapter = adapter

        // Verificamos si mostrar el mensaje de "No hay medicamentos"
        checkEmptyList()

        // Botón para ir a la pantalla de añadir, pasando la lista actual
        binding.AddButton.setOnClickListener {
            val intent = Intent(this, AddPill::class.java)
            intent.putExtra("Current_Pills", myPills)
            getPillResult.launch(intent)
        }

        // Botón para ir a la pantalla "Sobre nosotros"
        binding.About.setOnClickListener {
            val intent = Intent(this, AboutUs::class.java)
            startActivity(intent)
        }
        
        binding.CloseButton.setOnClickListener {
            finishAffinity()
        }
    }

    // Guarda la lista en un bundle temporal antes de destruir la actividad (ej: al rotar)
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("saved_pills", myPills)
    }

    // Alterna la visibilidad entre el mensaje de lista vacía y el listado real
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
