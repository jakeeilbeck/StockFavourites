package com.android.stockfavourites.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface StockDAO {
    @Insert
    suspend fun insert(quote: StockTable)

    @Update
    suspend fun update(quote: StockTable)

    @Delete
    suspend fun delete(quote: StockTable)

    @Query("SELECT count(symbol) FROM StockTable WHERE symbol = :symbolToCheck")
    suspend fun checkExists(symbolToCheck: String?): Int

    @Query("SELECT * FROM StockTable ORDER BY symbol ASC")
    fun getAll(): LiveData<List<StockTable>>
}