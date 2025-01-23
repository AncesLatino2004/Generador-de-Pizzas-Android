package com.epia.pizzas2025

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.epia.pizzas2025.adapters.PizzaAdapter
import com.epia.pizzas2025.room.Pizza
import com.epia.pizzas2025.room.PizzaDao
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class MainActivity : AppCompatActivity() {

    private lateinit var adapter: PizzaAdapter
    private val pizzaList = mutableListOf<Pizza>()
    private lateinit var database: AppDatabase
    private lateinit var pizzaDao: PizzaDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "pizza-database"
        ).build()

        pizzaDao = database.pizzaDao()
        setSupportActionBar(findViewById(R.id.menu))

        adapter = PizzaAdapter(pizzaList)
        findViewById<RecyclerView>(R.id.recyclerview).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter


        }
        CoroutineScope(Dispatchers.IO).launch {
            val pizzasFromDb = pizzaDao.getAllPizzas()
            pizzaList.addAll(pizzasFromDb)
            runOnUiThread {
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.addpizza -> {
                val intent = Intent(this, AddPizzaActivity::class.java)
                startActivityForResult(intent, 100)  // Cambié el código de solicitud aquí
            }
            R.id.ordenar -> {
                pizzaList.sortBy { it.description }
                adapter.notifyDataSetChanged()
            }
            R.id.pi -> adapter.updateData(pizzaList.filter { it.type == "Pi" })
            R.id.pc -> adapter.updateData(pizzaList.filter { it.type == "Pc" })
            R.id.pv -> adapter.updateData(pizzaList.filter { it.type == "Pv" })
            R.id.to -> adapter.updateData(pizzaList.filter { it.type == "To" })
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            // Recupera la pizza del Intent
            val newPizza = data?.getParcelableExtra<Pizza>("NEW_PIZZA")

            newPizza?.let {
                // Guardar la nueva pizza en la base de datos
                CoroutineScope(Dispatchers.IO).launch {
                    pizzaDao.insertPizza(it)

                    // Agregar la pizza a la lista y actualizar el RecyclerView en el hilo principal
                    runOnUiThread {
                        pizzaList.add(it)
                        adapter.notifyItemInserted(pizzaList.size - 1)
                    }
                }
            }
        }
    }

}
