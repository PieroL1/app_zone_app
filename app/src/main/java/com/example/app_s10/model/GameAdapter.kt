package com.example.app_s10.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app_s10.R

class GameAdapter(private var juegos: List<Game>) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    inner class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvGenero: TextView = itemView.findViewById(R.id.tvGenero)
        val tvAnio: TextView = itemView.findViewById(R.id.tvAnio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_juego, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val juego = juegos[position]
        holder.tvNombre.text = juego.nombre
        holder.tvGenero.text = juego.genero
        holder.tvAnio.text = juego.anio.toString()
    }

    override fun getItemCount(): Int = juegos.size

    fun actualizarLista(nuevaLista: List<Game>) {
        juegos = nuevaLista
        notifyDataSetChanged()
    }
}
