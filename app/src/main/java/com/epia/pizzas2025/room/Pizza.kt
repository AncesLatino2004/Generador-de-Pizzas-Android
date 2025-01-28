package com.epia.pizzas2025.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "Pizza") // Nombre de la tabla
data class Pizza(
    @PrimaryKey val reference: String, // La clave primaria
    val description: String,
    val type: String,
    val priceWithoutTax: Double,
    val priceWithTax: Double
) : Parcelable
