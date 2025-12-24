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
                myPills.clear()
                myPills.addAll(it)
                adapter.notifyDataSetChanged()
                checkEmptyList()
                
                // GUARDAR DATOS EN DISCO
                DataManager.savePills(this, myPills)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPillListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // CARGAR DATOS DESDE EL DISCO
        myPills.addAll(DataManager.loadPills(this))

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

        adapter = PillAdapter(myPills)
        binding.pillsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.pillsRecyclerView.adapter = adapter

        checkEmptyList()

        binding.AddButton.setOnClickListener {
            val intent = Intent(this, AddPill::class.java)
            intent.putExtra("Current_Pills", myPills)
            getPillResult.launch(intent)
        }

        binding.About.setOnClickListener {
            val intent = Intent(this, AboutUs::class.java)
            startActivity(intent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("saved_pills", myPills)
    }

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
