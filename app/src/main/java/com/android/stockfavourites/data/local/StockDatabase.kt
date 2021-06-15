package com.android.stockfavourites.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [StockTable::class, CandleTable::class], version = 6, exportSchema = false)
@TypeConverters(CandleTypeConverters::class)
abstract class StockDatabase : RoomDatabase() {
    abstract val stockDAO: StockDAO
}