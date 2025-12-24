package com.example.proyecto_alarma_pm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_alarma_pm.databinding.ItemPillBinding

/**
 * Adaptador para el RecyclerView de medicamentos.
 * Su función es tomar la lista de objetos [Pill] y convertirlos en elementos visuales (tarjetas) en la pantalla.
 */
class PillAdapter(private val pillList: MutableList<Pill>) :
    RecyclerView.Adapter<PillAdapter.PillViewHolder>() {

    /**
     * Clase interna que contiene las referencias a las vistas de cada elemento de la lista.
     * Usa ViewBinding para acceder a los componentes de item_pill.xml.
     */
    class PillViewHolder(val binding: ItemPillBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Método que se encarga de crear la estructura visual de cada fila/tarjeta.
     * Se infla el layout 'item_pill.xml'.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PillViewHolder {
        // Inflamos el diseño de la tarjeta individual (item_pill.xml)
        val binding = ItemPillBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PillViewHolder(binding)
    }

    /**
     * Método que se encarga de "rellenar" la tarjeta con los datos de un medicamento específico.
     * Se llama cada vez que un elemento entra en la pantalla al hacer scroll.
     */
    override fun onBindViewHolder(holder: PillViewHolder, position: Int) {
        // Obtenemos el medicamento que corresponde a esta posición en la lista
        val pill = pillList[position]
        
        // Asignamos el nombre del medicamento al TextView correspondiente
        holder.binding.tvName.text = pill.name
        
        // Formateamos y asignamos la información de las tomas diarias
        holder.binding.tvInfo.text = "${pill.numAlarms} veces al día"
        
        // Asignamos la duración del tratamiento
        holder.binding.tvDuration.text = "Duración: ${pill.duration}"
    }

    /**
     * Indica al RecyclerView cuántos elementos hay en total en la lista.
     */
    override fun getItemCount(): Int = pillList.size
}
