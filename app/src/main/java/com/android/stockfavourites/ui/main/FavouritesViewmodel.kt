package com.android.stockfavourites.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.stockfavourites.data.StockDAO
import com.android.stockfavourites.data.StockTable
import com.android.stockfavourites.models.Quote
import com.android.stockfavourites.models.SymbolLookup
import com.android.stockfavourites.data.RetrofitService
import kotlinx.coroutines.launch
import java.lang.Exception

class FavouritesViewmodel(private val service: RetrofitService, private val stockDAO: StockDAO) : ViewModel() {

    private val key = "API_KEY"

    fun searchStock(symbol: String, companyName: String){
        viewModelScope.launch {
            try {
                val stock = service.getQuote(symbol, key)
                addToFavourites(symbol, companyName, stock)
            }catch(e: Exception){
                Log.i("ViewModel", "Exception $e")
            }
        }
    }

    suspend fun searchSymbol(symbol: String): SymbolLookup {
        return service.getSymbols(symbol, key)
    }

    private fun addToFavourites(symbol: String, companyName: String, stock: Quote){
        viewModelScope.launch {
            val newStock = StockTable(
                    symbol,
                    companyName,
                    stock.o.toString(),
                    stock.h.toString(),
                    stock.l.toString(),
                    stock.c.toString(),
                    stock.pc.toString(),
                    "%.2f".format(priceChangeCalculation(stock.pc, stock.c)),
                    "%.2f".format(priceChangePercent(stock.pc, stock.c)) + "%"
            )
            insertFavourite(newStock)
        }
    }

    private fun priceChangeCalculation(close: Double?, current: Double?): Double?{
            return close?.let { current?.minus(it) }
    }

    private fun priceChangePercent(close: Double?, current: Double?): Double?{
        return ((close?.let { current?.minus(it) })?.div(close))?.times(100)
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