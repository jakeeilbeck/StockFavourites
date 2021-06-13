package com.android.stockfavourites.data.remote

import com.android.stockfavourites.models.CandleData
import com.android.stockfavourites.models.Quote
import com.android.stockfavourites.models.SymbolLookup
import retrofit2.http.GET
import retrofit2.http.Query

interface StockApi {

    @GET("quote?")
    suspend fun getQuote(@Query("symbol") ticker: String, @Query("token") key: String): Quote

    @GET("search?")
    suspend fun getSymbols(@Query("q") keywords: String, @Query("token") key: String): SymbolLookup

    @GET("stock/candle?")
    suspend fun getCandles(@Query("symbol") ticker: String, @Query("resolution") resolution: String, @Query("from") from: String, @Query("to") to: String, @Query("token") key: String): CandleData

    companion object {
        const val BASE_URL = "https://finnhub.io/api/v1/"
    }
}