package com.android.stockfavourites.data

import com.android.stockfavourites.models.Quote
import com.android.stockfavourites.models.SymbolLookup
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface RetrofitService {

    @GET("quote?")
    suspend fun getQuote(@Query("symbol") ticker: String, @Query("token") key: String): Quote

    @GET("search?")
    suspend fun getSymbols(@Query("q") keywords: String, @Query("token") key: String): SymbolLookup

    companion object{
        private const val BASE_URL = "https://finnhub.io/api/v1/"
        private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        private val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        private val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

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