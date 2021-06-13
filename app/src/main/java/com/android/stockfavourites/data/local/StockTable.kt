package com.android.stockfavourites.data.local

import androidx.room.*

@Entity
data class StockTable(
        @PrimaryKey
        val symbol: String,
        val companyName: String,
        val `open`: Double?,
        val high: Double?,
        val low: Double?,
        val price: Double?,
        val previousClose: Double?,
        val change: String?,
        val changePercent: String?
)

data class PricesUpdate(
        val symbol: String,
        val `open`: Double?,
        val high: Double?,
        val low: Double?,
        val price: Double?,
        val previousClose: Double?,
        val change: String?,
        val changePercent: String?
)

@Entity
data class CandleTable (
        @PrimaryKey
        val symbol: String,
        @TypeConverters(CandleTypeConverters::class)
        val candleClose: List<Double?>?
)


data class StockAndCandle(
        @Embedded
        val stockTable: StockTable,
        @Relation(parentColumn = "symbol", entityColumn = "symbol")
        val candleTable: CandleTable?
)