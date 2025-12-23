package com.example.proyecto_alarma_pm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_alarma_pm.databinding.ItemPillBinding

class PillAdapter(private val pillList: MutableList<Pill>) :
    RecyclerView.Adapter<PillAdapter.PillViewHolder>() {

    class PillViewHolder(val binding: ItemPillBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PillViewHolder {
        val binding = ItemPillBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PillViewHolder, position: Int) {
        val pill = pillList[position]
        holder.binding.tvName.text = pill.name
        holder.binding.tvInfo.text = "${pill.numAlarms} veces al día"
        holder.binding.tvDuration.text = "Duración: ${pill.duration}"
    }

    override fun getItemCount(): Int = pillList.size
}
