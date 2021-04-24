package com.android.stockfavourites.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [StockTable::class], version = 3, exportSchema = false)
abstract class StockDatabase : RoomDatabase() {
    abstract val stockDAO: StockDAO
}