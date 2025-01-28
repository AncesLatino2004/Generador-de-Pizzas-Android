package com.epia.pizzas2025

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.epia.pizzas2025.room.Pizza
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddPizzaActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "PizzaPreferences"
    private val REFERENCE_KEY = "lastReferenceNumber"

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
            val type = spinner.selectedItem.toString()
            val description = etDescription.text.toString()
            val priceWithoutTax = etPrice.text.toString().toDoubleOrNull()
            val reference = etReference.text.toString()

            // Validaciones básicas
            if (type.isBlank() || description.isBlank() || priceWithoutTax == null || reference.isBlank()) {
                // Muestra un mensaje de error si falta algún campo
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Validar que la referencia sea coherente con el tipo
            val prefix = when (type) {
                "PIZZA" -> "PI"
                "PIZZA VEGANA" -> "PV"
                "PIZZA CELIACA" -> "PC"
                "TOPPING" -> "TO"
                else -> null
            }

            if (!reference.startsWith(prefix ?: "")) {
                Toast.makeText(this, "La referencia debe comenzar con $prefix", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Calcular el precio con IVA
            val priceWithTax = calculatePriceWithTax(priceWithoutTax)

            // Crear el objeto Pizza
            val pizza = Pizza(reference, type, description, priceWithoutTax, priceWithTax)

            // Guardar la pizza en la base de datos
            // Verificar si la referencia ya existe en la base de datos
            CoroutineScope(Dispatchers.IO).launch {
                val database = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java, "pizza-database"
                ).build()

                val existingPizza = database.pizzaDao().getPizzaByReference(reference)
                if (existingPizza != null) {
                    runOnUiThread {
                        Toast.makeText(
                            this@AddPizzaActivity,
                            "La referencia ya existe",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                // Si la referencia no existe, continúa guardando la pizza
                database.pizzaDao().insertPizza(pizza)
                runOnUiThread {
                    // Pasar el resultado de vuelta a la actividad principal
                    val resultIntent = Intent()
                    resultIntent.putExtra("NEW_PIZZA", pizza)
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }
        }
    }

        private fun calculatePriceWithTax(priceWithoutTax: Double): Double {
        val taxRate = 0.21 // Ejemplo: 21% de IVA
        return priceWithoutTax * (1 + taxRate)
    }
}
