package com.epia.pizzas2025.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.epia.pizzas2025.R
import com.epia.pizzas2025.room.Pizza

class PizzaAdapter(private var pizzaList: MutableList<Pizza>) :
    RecyclerView.Adapter<PizzaAdapter.PizzaViewHolder>() {

    class PizzaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvReferencia: TextView = itemView.findViewById(R.id.Referencia)
        val tvDescription: TextView = itemView.findViewById(R.id.Descripcio)
        val tvType: TextView = itemView.findViewById(R.id.Tipus)
        val tvPriceWithoutTax: TextView = itemView.findViewById(R.id.sesnseiva)
        val tvPriceWithTax: TextView = itemView.findViewById(R.id.iva)
        val btnDelete: ImageView = itemView.findViewById(R.id.Eliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PizzaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lista_row, parent, false)
        return PizzaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PizzaViewHolder, position: Int) {
        val pizza = pizzaList[position]

        holder.tvReferencia.text = pizza.reference
        holder.tvDescription.text = pizza.description
        holder.tvType.text = pizza.type
        holder.tvPriceWithoutTax.text = pizza.priceWithoutTax.toString()
        holder.tvPriceWithTax.text = pizza.priceWithTax.toString()

        holder.btnDelete.setOnClickListener {
            pizzaList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount(): Int = pizzaList.size

    // Método para actualizar los datos
    fun updateData(newList: List<Pizza>) {
        pizzaList.clear()
        pizzaList.addAll(newList)
        notifyDataSetChanged()
    }
}
