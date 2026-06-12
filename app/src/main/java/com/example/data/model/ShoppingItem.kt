package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // e.g., "Frutta e Verdura", "Latticini e Uova", etc.
    val quantity: Int = 1,
    val unit: String = "", // e.g., "kg", "g", "pz", "pacco/hi"
    val notes: String = "",
    val isCompleted: Boolean = false,
    val price: Double = 1.0,
    val emoji: String = "📦",
    val createdAt: Long = System.currentTimeMillis(),
    val imagePath: String? = null
) : Serializable
