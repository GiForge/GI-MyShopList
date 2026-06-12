package com.example.data.repository

import com.example.data.local.ShoppingDao
import com.example.data.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

class ShoppingRepository(private val shoppingDao: ShoppingDao) {
    val allItems: Flow<List<ShoppingItem>> = shoppingDao.getAllItems()

    suspend fun insert(item: ShoppingItem): Long {
        return shoppingDao.insertItem(item)
    }

    suspend fun update(item: ShoppingItem) {
        shoppingDao.updateItem(item)
    }

    suspend fun delete(item: ShoppingItem) {
        shoppingDao.deleteItem(item)
    }

    suspend fun deleteCompleted() {
        shoppingDao.deleteCompletedItems()
    }

    suspend fun deleteAll() {
        shoppingDao.deleteAllItems()
    }
}
