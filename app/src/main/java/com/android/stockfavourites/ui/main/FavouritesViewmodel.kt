package com.android.stockfavourites.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.stockfavourites.data.StockDAO
import com.android.stockfavourites.data.StockTable
import com.android.stockfavourites.Models.Quote
import com.android.stockfavourites.Models.SymbolMatch
import com.android.stockfavourites.data.RetrofitService
import kotlinx.coroutines.launch
import java.lang.Exception

class FavouritesViewmodel(private val service: RetrofitService, private val stockDAO: StockDAO) : ViewModel() {

    private val key = "API_KEY"

    fun searchStock(symbol: String){
        val function = "GLOBAL_QUOTE"
        viewModelScope.launch {
            try {
                val stock = service.getQuote(function, symbol, key)
                if (stock.globalQuote?.symbol != null){
                    addToFavourites(stock)
                }

            }catch(e: Exception){
                Log.i("ViewModel", "Exception $e")
            }
        }
    }

    //make a job so can cancel previous job after each character entry
    suspend fun searchSymbol(symbol: String): SymbolMatch{
        val function = "SYMBOL_SEARCH"
        return service.getSymbols(function, symbol, key)
    }

    private fun addToFavourites(stock: Quote){
        viewModelScope.launch {
            val newStock = StockTable(
                    stock.globalQuote!!.symbol,
                    stock.globalQuote.open,
                    stock.globalQuote.high,
                    stock.globalQuote.low,
                    stock.globalQuote.price,
                    stock.globalQuote.previousClose,
                    stock.globalQuote.change,
                    stock.globalQuote.changePercent
            )
            insertFavourite(newStock)
        }
    }

    private fun insertFavourite(stock: StockTable){
        viewModelScope.launch {
            if (!checkExists(stock.symbol)){
                stockDAO.insert(stock)
            }else{
                updateExisting(stock)
            }
        }
    }

    private fun updateExisting(stock: StockTable){
        viewModelScope.launch {
            stockDAO.update(stock)
        }
    }

    fun deleteStock(quote: StockTable){
        viewModelScope.launch {
            stockDAO.delete(quote)
        }
    }

    private suspend fun checkExists(symbol: String?): Boolean{
            return stockDAO.checkExists(symbol) == 1
    }

    fun getAllFavourites(): LiveData<List<StockTable>>{
        return stockDAO.getAll()
    }
}