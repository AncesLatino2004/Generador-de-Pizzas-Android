package com.epia.pizzas2025.room

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "Pizza")
data class Pizza(
    @PrimaryKey val reference: String,
    val type: String,
    val description: String,
    val priceWithoutTax: Double,
    val priceWithTax: Double
) : Parcelable

