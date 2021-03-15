package com.android.stockfavourites.data

import com.android.stockfavourites.Models.Quote
import com.android.stockfavourites.Models.SymbolMatch
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {

    @GET("query?")
    suspend fun getQuote(@Query("function") function: String, @Query("symbol") ticker: String, @Query("apikey") key: String): Quote

    @GET("query?")
    suspend fun getSymbols(@Query("function") function: String, @Query("keywords") keywords: String, @Query("apikey") key: String): SymbolMatch

    companion object{
        private const val BASE_URL = "https://www.alphavantage.co/"
        private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        private val okHttpClient = OkHttpClient.Builder().addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        ).build()

        fun create(): RetrofitService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(okHttpClient)
                .build()
                .create(RetrofitService::class.java)
        }
    }
}