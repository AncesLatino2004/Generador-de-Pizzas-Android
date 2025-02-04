package com.epia.pizzas2025

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.room.Room
import com.epia.pizzas2025.room.Pizza
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddPizzaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addpizza_layout)

        val spinner = findViewById<Spinner>(R.id.spinner_type)
        val etDescription = findViewById<EditText>(R.id.et_description)
        val etPrice = findViewById<EditText>(R.id.et_price)
        val etReference = findViewById<EditText>(R.id.et_referencia)
        val btnSave = findViewById<Button>(R.id.btn_save)

        btnSave.setOnClickListener {
            // Obtener los datos del usuario
            val type = spinner.selectedItem?.toString()?.trim() ?: ""
            val description = etDescription.text?.toString()?.trim() ?: ""
            val priceWithoutTax = etPrice.text?.toString()?.toDoubleOrNull()
            val reference = etReference.text?.toString()?.trim() ?: ""

            // Validaciones básicas
            if (type.isBlank() || description.isBlank() || priceWithoutTax == null || reference.isBlank()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar el tipo antes de usarlo
            val prefix = when (type) {
                "PIZZA" -> "PI"
                "PIZZA VEGANA" -> "PV"
                "PIZZA CELIACA" -> "PC"
                "TOPPING" -> "TO"
                else -> {
                    Toast.makeText(this, "Tipo de pizza no válido", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Validar que la referencia comience con el prefijo correcto
            if (!reference.startsWith(prefix)) {
                Toast.makeText(this, "La referencia debe comenzar con $prefix", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Calcular el precio con IVA
            val priceWithTax = calculatePriceWithTax(priceWithoutTax)

            // Crear el objeto Pizza
            val pizza = Pizza(reference, description, type, priceWithoutTax, priceWithTax)

            // Guardar en la base de datos de manera segura
            CoroutineScope(Dispatchers.IO).launch {
                val database = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java, "pizza-database"
                ).build()

                val existingPizza = database.pizzaDao().getPizzaByReference(reference)
                if (existingPizza != null) {
                    runOnUiThread {
                        Toast.makeText(this@AddPizzaActivity, "La referencia ya existe", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // Insertar la pizza en la base de datos
                database.pizzaDao().insertPizza(pizza)
                runOnUiThread {
                    val resultIntent = Intent()
                    resultIntent.putExtra("NEW_PIZZA", pizza)
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }
        }
    }

    private fun calculatePriceWithTax(priceWithoutTax: Double): Double {
        val taxRate = 0.21 // IVA 21%
        return priceWithoutTax * (1 + taxRate)
    }
}
