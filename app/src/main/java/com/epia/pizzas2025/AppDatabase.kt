
package com.epia.pizzas2025

import androidx.room.Database
import androidx.room.RoomDatabase
import com.epia.pizzas2025.room.Pizza
import com.epia.pizzas2025.room.PizzaDao

@Database(entities = [Pizza::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pizzaDao(): PizzaDao
}
