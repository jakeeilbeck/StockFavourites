package com.android.stockfavourites.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.stockfavourites.data.*
import com.android.stockfavourites.data.local.CandleTable
import com.android.stockfavourites.data.local.StockAndCandle
import com.android.stockfavourites.data.local.StockTable
import com.android.stockfavourites.models.SymbolLookup
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.*
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor (private val repository: StockRepository) :
    ViewModel() {

    val errorFlow = MutableSharedFlow<String>(0)
    val loadingFlow = MutableStateFlow(false)
    private val secondsInDay = 86400

    private fun setLoadingStatus(isLoading: Boolean){
        viewModelScope.launch {
            loadingFlow.emit(isLoading)
        }
    }

    //Get stock details once autocomplete item selected
    fun getStock(symbol: String, companyName: String) {
        viewModelScope.launch {
            try {
                repository.getStock(symbol, companyName)
                insertDailyCandle(symbol)
            } catch (e: Exception) {
                errorFlow.emit("Error searching stock")
                Log.i("ViewModel.searchStock", "Exception: $e")
            }
        }
    }

    //Search symbols for autocomplete
    suspend fun searchSymbol(symbol: String): SymbolLookup? =
        try {
            repository.searchSymbol(symbol)
        } catch (e: Exception){
            errorFlow.emit("Error searching symbols")
            Log.i("ViewModel.searchSymbol", "Exception: $e")
            null
        }

    fun deleteStock(quote: StockTable) {
        viewModelScope.launch {
            repository.deleteFavourite(quote)
        }
    }

    fun getAllFavourites(): LiveData<List<StockTable>> {
        return repository.getAllFavourites()
    }

    fun updateAllFavourites() {
        viewModelScope.launch {
            try {
                setLoadingStatus(true)
                repository.updateAllFavourites()
                updateDailyCandles()
                setLoadingStatus(false)
                errorFlow.emit("Stocks updated")
            }catch (e: Exception){
                setLoadingStatus(false)
                errorFlow.emit("Error updating stocks")
                Log.i("ViewModel.updateAll", "Exception: $e")
            }
        }
    }

    //Get data for graphs - data points in 5 minute intervals
    private suspend fun updateDailyCandles() {
        //API requires Unix time, so here we get the Unix time at the start and end of day
        //If it is the weekend we get Friday timestamps

        var dayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).toEpochSecond(ZoneOffset.UTC)
        var dayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).toEpochSecond(ZoneOffset.UTC)
        val day = LocalDate.now().dayOfWeek
        val resolution = "5"

        if (day == DayOfWeek.SATURDAY){
            dayStart -= secondsInDay
            dayEnd -= secondsInDay
        }else if(day == DayOfWeek.SUNDAY){
            dayStart -= (secondsInDay * 2)
            dayEnd -= (secondsInDay * 2)
        }

        for (symbol in repository.getSymbols()) {
            val candleData = repository.getCandles(symbol, resolution, dayStart.toString(), dayEnd.toString())
            if (candleData.s.equals("ok")){
                val stock = CandleTable(
                    symbol,
                    candleData.c
                )
                repository.insertCandleData(stock)
            }
        }
    }

    private suspend fun insertDailyCandle(symbol: String){
        var dayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).toEpochSecond(ZoneOffset.UTC)
        var dayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).toEpochSecond(ZoneOffset.UTC)
        val day = LocalDate.now().dayOfWeek
        val resolution = "5"

        if (day == DayOfWeek.SATURDAY){
            dayStart -= secondsInDay
            dayEnd -= secondsInDay
        }else if(day == DayOfWeek.SUNDAY){
            dayStart -= (secondsInDay * 2)
            dayEnd -= (secondsInDay * 2)
        }

        val candleData = repository.getCandles(symbol, resolution, dayStart.toString(), dayEnd.toString())

        if (candleData.s.equals("ok")) {
            val stock = CandleTable(
                symbol,
                candleData.c
            )
            repository.insertCandleData(stock)
        }
    }

    fun getAllStockAndCandle(): LiveData<List<StockAndCandle>>{
        return repository.getStockAndCandleData()
    }
}