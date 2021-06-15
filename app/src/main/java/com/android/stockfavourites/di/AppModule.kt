package com.android.stockfavourites.di

import android.content.Context
import androidx.room.Room
import com.android.stockfavourites.data.remote.StockApi
import com.android.stockfavourites.data.local.StockDAO
import com.android.stockfavourites.data.local.StockDatabase
import com.android.stockfavourites.data.StockRepository
import com.android.stockfavourites.ui.main.RecyclerViewAdapter
import com.android.stockfavourites.ui.main.SparkChartAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    //Retrofit dependencies
    @Provides
    @Singleton
    fun provideRetrofitService(retrofit: Retrofit): StockApi =
        retrofit.create(StockApi::class.java)

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(StockApi.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Provides
    @Singleton
    fun provideOkHttpClient(logging: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

    @Provides
    @Singleton
    fun provideHttpLogging(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }


    //Repository dependencies
    @Provides
    @Singleton
    fun provideRepository(stockApi: StockApi, stockDAO: StockDAO): StockRepository =
        StockRepository(stockApi, stockDAO)

    @Provides
    @Singleton
    fun provideRoomDatabase(@ApplicationContext context: Context): StockDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            StockDatabase::class.java,
            "StockTable"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideDAO(stockDatabase: StockDatabase): StockDAO =
        stockDatabase.stockDAO


    //Recyclerview adapter dependency
    @Provides
    @Singleton
    fun provideRecyclerViewAdapter(@ApplicationContext context: Context, sparkChartAdapter: SparkChartAdapter): RecyclerViewAdapter =
        RecyclerViewAdapter(context, sparkChartAdapter)

    //SparkChart adapter dependency
    @Provides
    @Singleton
    fun provideSparkChartAdapter(): SparkChartAdapter =
        SparkChartAdapter()
}