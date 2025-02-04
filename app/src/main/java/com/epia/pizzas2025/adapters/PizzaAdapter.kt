
package com.epia.pizzas2025.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.epia.pizzas2025.R
import com.epia.pizzas2025.room.Pizza

class PizzaAdapter(
    private var pizzaList: MutableList<Pizza>,
    private val onDelete: (Pizza) -> Unit,
    private val onEdit: (Pizza) -> Unit // Nuevo callback
) : RecyclerView.Adapter<PizzaAdapter.PizzaViewHolder>() {

    class PizzaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvReferencia: TextView = itemView.findViewById(R.id.Referencia)
        val tvDescription: TextView = itemView.findViewById(R.id.Descripcio)
        val tvType: TextView = itemView.findViewById(R.id.Tipus)
        val tvPriceWithoutTax: TextView = itemView.findViewById(R.id.sesnseiva)
        val tvPriceWithTax: TextView = itemView.findViewById(R.id.iva)
        val btnDelete: ImageView = itemView.findViewById(R.id.Eliminar)
        val card: CardView = itemView.findViewById(R.id.card)
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
        holder.tvReferencia.setOnClickListener {
            onEdit(pizza) // Llamar al callback para editar
        }
        holder.btnDelete.setOnClickListener {
            val removedPizza = pizzaList[position]
            pizzaList.removeAt(position)
            notifyItemRemoved(position)
            onDelete(removedPizza) // Llama al callback para eliminarla de Room
        }
        val backgroundColor = when (pizza.type) {
            "PIZZA" -> ContextCompat.getColor(holder.itemView.context, R.color.ColorPI) // Color para Pizza
            "PIZZA VEGANA" -> ContextCompat.getColor(holder.itemView.context, R.color.ColorPV) // Color para Pizza Vegana
            "PIZZA CELIACA" -> ContextCompat.getColor(holder.itemView.context, R.color.ColorPC) // Color para Pizza Celiaca
            "TOPPING" -> ContextCompat.getColor(holder.itemView.context, R.color.ColorTO) // Color para Topping
            else -> ContextCompat.getColor(holder.itemView.context, R.color.white) // Default color
        }

        // Aplicar el color de fondo a la card
        holder.card.setCardBackgroundColor(backgroundColor)

        Log.d("PizzaAdapter", "Referencia: ${pizza.reference}, Descripción: ${pizza.description}, Tipo: ${pizza.type}")

    }

    override fun getItemCount(): Int = pizzaList.size

    fun updateData(newList: List<Pizza>) {
        pizzaList.clear()
        pizzaList.addAll(newList)
        notifyDataSetChanged()
    }
}
