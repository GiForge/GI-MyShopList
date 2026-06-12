package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ShoppingDatabase
import com.example.data.model.ShoppingItem
import com.example.data.repository.ShoppingRepository
import com.example.ui.parser.ItalianVocalParser
import org.json.JSONArray
import org.json.JSONObject
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoppingViewModel(
    application: Application,
    private val repository: ShoppingRepository
) : AndroidViewModel(application) {

    val items: StateFlow<List<ShoppingItem>> = repository.allItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, ShoppingItem::class.java)
    private val jsonAdapter = moshi.adapter<List<ShoppingItem>>(listType)

    fun addParsedInput(input: String) {
        if (input.isBlank()) return
        viewModelScope.launch {
            val parsedItems = ItalianVocalParser.parseInput(input)
            for (item in parsedItems) {
                repository.insert(item)
            }
        }
    }

    fun addItemManually(name: String, category: String, quantity: Int, unit: String, notes: String, customPrice: Double? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val resolved = ItalianVocalParser.resolveFromCatalog(name)
            val finalCategory = if (category == "Altro" || category.isBlank()) resolved.category else category
            val finalPrice = customPrice ?: resolved.price
            val item = ShoppingItem(
                name = name.trim(),
                category = finalCategory,
                quantity = quantity,
                unit = unit.trim(),
                notes = notes.trim(),
                price = finalPrice,
                emoji = resolved.emoji
            )
            repository.insert(item)
        }
    }

    fun toggleItemCompletion(item: ShoppingItem) {
        viewModelScope.launch {
            repository.update(item.copy(isCompleted = !item.isCompleted))
        }
    }

    fun updateItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.update(item)
        }
    }

    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }

    fun clearCompleted() {
        viewModelScope.launch {
            repository.deleteCompleted()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    // JSON export
    fun exportToJson(): String {
        return try {
            jsonAdapter.toJson(items.value)
        } catch (e: Exception) {
            ""
        }
    }

    // JSON import
    fun importFromJson(jsonStr: String): Boolean {
        return try {
            val trimmed = jsonStr.trim()
            if (!trimmed.startsWith("[")) return false
            val jsonArray = JSONArray(trimmed)
            val listToInsert = mutableListOf<ShoppingItem>()
            
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                
                // 1. Get name (supports many synonyms in standard formats)
                val name = optString(obj, "name", "nome", "prodotto", "title", "titolo", "item", "descrizione") ?: continue
                if (name.isBlank()) continue
                
                // Fetch catalog defaults to fill in any gaps (category, price, emoji)
                val resolved = ItalianVocalParser.resolveFromCatalog(name)
                
                // 2. Get category
                var category = optString(obj, "category", "categoria", "cat", "gruppo") ?: ""
                if (category.isBlank() || category == "Altro") {
                    category = resolved.category
                }
                
                // 3. Get quantity
                val quantity = optInt(obj, 1, "quantity", "quantità", "quantita", "qty", "qta", "qtà")
                
                // 4. Get unit
                val unit = optString(obj, "unit", "unità", "unita", "misura") ?: ""
                
                // 5. Get notes
                val notes = optString(obj, "notes", "note", "commento", "info") ?: ""
                
                // 6. Get isCompleted
                val isCompleted = optBoolean(obj, false, "isCompleted", "completato", "completed", "checked", "is_completed")
                
                // 7. Get price
                val price = optDouble(obj, resolved.price, "price", "prezzo", "costo", "cost", "valore")
                
                // 8. Get emoji
                var emoji = optString(obj, "emoji", "icon", "icona", "avatar") ?: ""
                if (emoji.isBlank() || emoji == "📦") {
                    emoji = resolved.emoji
                }

                // 9. Get imagePath
                val imagePath = optString(obj, "imagePath", "image_path", "image", "img")
                
                val newItem = ShoppingItem(
                    name = name.trim(),
                    category = category,
                    quantity = quantity,
                    unit = unit.trim(),
                    notes = notes.trim(),
                    isCompleted = isCompleted,
                    price = price,
                    emoji = emoji,
                    imagePath = imagePath
                )
                listToInsert.add(newItem)
            }
            
            if (listToInsert.isEmpty()) return false
            
            viewModelScope.launch {
                for (item in listToInsert) {
                    repository.insert(item)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun optString(obj: JSONObject, vararg keys: String): String? {
        for (key in keys) {
            if (obj.has(key)) {
                val value = obj.opt(key)
                if (value != null && value != JSONObject.NULL) {
                    return value.toString()
                }
            }
        }
        return null
    }

    private fun optInt(obj: JSONObject, default: Int, vararg keys: String): Int {
        for (key in keys) {
            if (obj.has(key)) {
                val value = obj.opt(key)
                if (value is Number) {
                    return value.toInt()
                } else if (value is String) {
                    val parsed = value.toIntOrNull()
                    if (parsed != null) return parsed
                }
            }
        }
        return default
    }

    private fun optDouble(obj: JSONObject, default: Double, vararg keys: String): Double {
        for (key in keys) {
            if (obj.has(key)) {
                val value = obj.opt(key)
                if (value is Number) {
                    return value.toDouble()
                } else if (value is String) {
                    val parsed = value.toDoubleOrNull()
                    if (parsed != null) return parsed
                }
            }
        }
        return default
    }

    private fun optBoolean(obj: JSONObject, default: Boolean, vararg keys: String): Boolean {
        for (key in keys) {
            if (obj.has(key)) {
                val value = obj.opt(key)
                if (value is Boolean) {
                    return value
                } else if (value is String) {
                    val lower = value.lowercase().trim()
                    if (lower == "true" || lower == "1" || lower == "sì" || lower == "si" || lower == "yes") return true
                    if (lower == "false" || lower == "0" || lower == "no") return false
                } else if (value is Number) {
                    return value.toInt() != 0
                }
            }
        }
        return default
    }

    // Factory provider for clean MVVM construction
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShoppingViewModel::class.java)) {
                val database = ShoppingDatabase.getDatabase(application)
                val repository = ShoppingRepository(database.shoppingDao())
                return ShoppingViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
