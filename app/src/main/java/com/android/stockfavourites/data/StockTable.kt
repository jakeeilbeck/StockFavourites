package com.android.stockfavourites.data

import androidx.room.*

@Entity
data class StockTable(
        @PrimaryKey
        val symbol: String,
        val `open`: String?,
        val high: String?,
        val low: String?,
        val price: String?,
        val previousClose: String?,
        val change: String?,
        val changePercent: String?
)