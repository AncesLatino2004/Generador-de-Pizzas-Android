package com.epia.pizzas2025.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PizzaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertPizza(pizza: Pizza)

    @Query("SELECT * FROM Pizza ORDER BY reference ASC")
     fun getAllPizzas(): MutableList<Pizza>

    @Query("SELECT reference FROM Pizza WHERE type = :type ORDER BY reference DESC LIMIT 1")
     fun getLastReferenceByType(type: String): String?

    @Query("SELECT * FROM Pizza WHERE reference = :reference LIMIT 1")
    fun getPizzaByReference(reference: String): Pizza?
    @Delete
    fun deletePizza(pizza: Pizza)

    @Update
    fun updatePizza(pizza: Pizza)
}
