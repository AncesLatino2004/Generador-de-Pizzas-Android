package com.epia.pizzas2025

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.epia.pizzas2025.room.Pizza
import com.epia.pizzas2025.room.PizzaDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditPizzaActivity : AppCompatActivity() {

    private lateinit var pizzaDao: PizzaDao
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pizza)

        val database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "pizza-database").build()
        pizzaDao = database.pizzaDao()
        sharedPreferences = getSharedPreferences("PizzaPreferences", Context.MODE_PRIVATE)

        val reference = intent.getStringExtra("REFERENCE")
        val pizza = intent.getParcelableExtra<Pizza>("PIZZA")

        val tvReference = findViewById<TextView>(R.id.tv_reference)
        val etDescription = findViewById<EditText>(R.id.et_edit_description)
        val etPrice = findViewById<EditText>(R.id.et_edit_price)
        val btnSave = findViewById<Button>(R.id.btn_save_changes)

        pizza?.let {
            tvReference.text = "Referencia: ${it.reference}"
            etDescription.setText(it.description)
            etPrice.setText(it.priceWithoutTax.toString())

            btnSave.setOnClickListener {
                val newDescription = etDescription.text.toString()
                val newPriceWithoutTax = etPrice.text.toString().toDoubleOrNull()

                if (newPriceWithoutTax != null) {
                    val taxRate = sharedPreferences.getFloat("taxRate", 21f)
                    val newPriceWithTax = newPriceWithoutTax * (1 + taxRate / 100)

                    CoroutineScope(Dispatchers.IO).launch {
                        val updatedPizza = Pizza(
                            reference = pizza.reference,
                            description = newDescription,
                            type = pizza.type,
                            priceWithoutTax = newPriceWithoutTax,
                            priceWithTax = newPriceWithTax
                        )
                        pizzaDao.updatePizza(updatedPizza)

                        // Pasar el resultado de vuelta a la actividad principal
                        val resultIntent = Intent().apply {
                            putExtra("UPDATED_PIZZA", updatedPizza)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                }
            }

        }
    }
}
