package com.epia.pizzas2025

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.epia.pizzas2025.adapters.PizzaAdapter
import com.epia.pizzas2025.room.Pizza
import com.epia.pizzas2025.room.PizzaDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: PizzaAdapter
    private val pizzaList = mutableListOf<Pizza>()
    private lateinit var database: AppDatabase
    private lateinit var pizzaDao: PizzaDao
    private var isAscending = true // Estado de orden (A-Z o Z-A)
    private var selectedFilter: String? = null // Filtro actual
    private var originalList: List<Pizza> = listOf() // Lista completa sin filtros

    companion object {
        const val REQUEST_CODE_ADD_PIZZA = 100
        const val REQUEST_CODE_EDIT_PIZZA = 101
        const val REQUEST_CODE_CHANGE_TAX = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar la base de datos Room
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "pizza-database"
        ).build()

        pizzaDao = database.pizzaDao()
        setSupportActionBar(findViewById(R.id.menu))

        // Inicializar el adaptador con la funcionalidad de eliminar y editar
        adapter = PizzaAdapter(
            pizzaList,
            onDelete = { pizzaToDelete ->
                CoroutineScope(Dispatchers.IO).launch {
                    pizzaDao.deletePizza(pizzaToDelete)
                    // Actualizar la lista después de eliminar
                    val updatedPizzaList = pizzaDao.getAllPizzas()
                    runOnUiThread {
                        pizzaList.clear()
                        pizzaList.addAll(updatedPizzaList)
                        adapter.notifyDataSetChanged()
                    }
                }
            },
            onEdit = { pizzaToEdit ->
                val intent = Intent(this, EditPizzaActivity::class.java)
                intent.putExtra("PIZZA", pizzaToEdit)
                startActivityForResult(intent, REQUEST_CODE_EDIT_PIZZA)
            }
        )

        // Configurar el RecyclerView
        findViewById<RecyclerView>(R.id.recyclerview).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        // Cargar las pizzas desde la base de datos Room
        CoroutineScope(Dispatchers.IO).launch {
            val pizzasFromDb = pizzaDao.getAllPizzas()
            pizzaList.addAll(pizzasFromDb)
            originalList = pizzasFromDb // Asignar las pizzas a originalList
            runOnUiThread {
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    private fun updateList() {
        var filteredList = originalList

        // Aplicar filtro de tipo si hay uno seleccionado
        selectedFilter?.let { filter ->
            filteredList = filteredList.filter { it.type == filter }
        }

        // Aplicar orden (A-Z o Z-A)
        filteredList = if (isAscending) {
            filteredList.sortedBy { it.description }
        } else {
            filteredList.sortedByDescending { it.description }
        }

        // Verificar si filteredList está vacía
        if (filteredList.isEmpty()) {
            println("No hay pizzas disponibles para el filtro seleccionado.")
        }

        // Actualizar el adaptador con la lista filtrada y ordenada
        adapter.updateData(filteredList)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.addpizza -> {
                val intent = Intent(this, AddPizzaActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_ADD_PIZZA)
            }

            R.id.ordenar -> {
                isAscending = !isAscending
                updateList() // Aplicar orden y filtro a la vez
            }

            R.id.pi -> {

                selectedFilter = if (selectedFilter == "PIZZA") null else "PIZZA"
                updateList() // Aplicar filtro y orden
            }

            R.id.pc -> {
                selectedFilter = if (selectedFilter == "PIZZA CELIACA") null else "PIZZA CELIACA"
                updateList() // Aplicar filtro y orden
            }

            R.id.pv -> {
                selectedFilter = if (selectedFilter == "PIZZA VEGANA") null else "PIZZA VEGANA"
                updateList() // Aplicar filtro y orden
            }

            R.id.to -> {
                selectedFilter = if (selectedFilter == "TOPPING") null else "TOPPING"
                updateList() // Aplicar filtro y orden
            }

            R.id.configure_tax -> {
                val intent = Intent(this, ConfigureTaxActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_CHANGE_TAX)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_CHANGE_TAX -> {
                    // Cambiar el IVA (requestCode 200)
                    val newTax = data?.getFloatExtra("NEW_TAX", 21f) ?: return

                    // Actualizar los precios con el nuevo IVA
                    CoroutineScope(Dispatchers.IO).launch {
                        val pizzas = pizzaDao.getAllPizzas()
                        pizzas.forEach { pizza ->
                            val updatedPriceWithTax = pizza.priceWithoutTax * (1 + newTax / 100)
                            pizzaDao.updatePizza(pizza.copy(priceWithTax = updatedPriceWithTax))
                        }

                        // Actualizar la UI después de cambiar el IVA
                        runOnUiThread {
                            pizzaList.clear()
                            pizzaList.addAll(pizzas)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }

                REQUEST_CODE_EDIT_PIZZA -> {
                    // Si es la edición de una pizza (requestCode 101)
                    val updatedPizza = data?.getParcelableExtra<Pizza>("UPDATED_PIZZA")

                    updatedPizza?.let {
                        CoroutineScope(Dispatchers.IO).launch {
                            pizzaDao.updatePizza(it)
                            val updatedPizzaList = pizzaDao.getAllPizzas()
                            runOnUiThread {
                                pizzaList.clear()
                                pizzaList.addAll(updatedPizzaList)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }

                REQUEST_CODE_ADD_PIZZA -> {
                    // Si es agregar una nueva pizza (requestCode 100)
                    val newPizza = data?.getParcelableExtra<Pizza>("NEW_PIZZA")

                    newPizza?.let {
                        CoroutineScope(Dispatchers.IO).launch {
                            // Insertar la nueva pizza en la base de datos
                            pizzaDao.insertPizza(it)

                            // Obtener la lista actualizada de pizzas desde la base de datos
                            val updatedPizzaList = pizzaDao.getAllPizzas()

                            // Actualizar la lista original (originalList) con las pizzas actualizadas
                            originalList = updatedPizzaList

                            // Actualizar la lista del RecyclerView en el hilo principal
                            runOnUiThread {
                                pizzaList.clear() // Limpiar la lista antes de agregar las pizzas actualizadas
                                pizzaList.addAll(updatedPizzaList) // Agregar las pizzas actualizadas
                                adapter.notifyDataSetChanged() // Notificar al adaptador sobre los cambios
                            }
                        }
                    }
                }

            }
        }
    }
}
