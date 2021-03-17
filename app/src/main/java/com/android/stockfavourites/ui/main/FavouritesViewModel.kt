package com.android.stockfavourites.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.stockfavourites.data.PricesUpdate
import com.android.stockfavourites.data.RetrofitService
import com.android.stockfavourites.data.StockDAO
import com.android.stockfavourites.data.StockTable
import com.android.stockfavourites.models.Quote
import com.android.stockfavourites.models.SymbolLookup
import kotlinx.coroutines.launch

class FavouritesViewModel(private val service: RetrofitService, private val stockDAO: StockDAO) :
    ViewModel() {

    private val key = "API_KEY"

    fun searchStock(symbol: String, companyName: String) {
        viewModelScope.launch {
            try {
                val stock = service.getQuote(symbol, key)
                addToFavourites(symbol, companyName, stock)
            } catch (e: Exception) {
                Log.i("ViewModel.searchStock", "Exception $e")
            }
        }
    }

    suspend fun searchSymbol(symbol: String): SymbolLookup {
        return service.getSymbols(symbol, key)
    }

    private fun addToFavourites(symbol: String, companyName: String, stock: Quote) {
        viewModelScope.launch {
            val newStock = StockTable(
                symbol,
                companyName,
                stock.o,
                stock.h,
                stock.l,
                stock.c,
                stock.pc,
                formatDecimalPlaces(priceChangeCalculation(stock.pc, stock.c)),
                formatDecimalPlaces(priceChangePercentCalculation(stock.pc, stock.c)) + "%"
            )
            insertFavourite(newStock)
        }
    }

    private fun formatDecimalPlaces(number: Double?): String {
        return "%.2f".format(number)
    }

    private fun priceChangeCalculation(close: Double?, current: Double?): Double? {
        return close?.let { current?.minus(it) }
    }

    private fun priceChangePercentCalculation(close: Double?, current: Double?): Double? {
        return ((close?.let { current?.minus(it) })?.div(close))?.times(100)
    }

    private fun insertFavourite(stock: StockTable) {
        viewModelScope.launch {
            if (!checkExists(stock.symbol)) {
                stockDAO.insert(stock)
            } else {
                updateExisting(stock)
            }
        }
    }

    private fun updateExisting(stock: StockTable) {
        viewModelScope.launch {
            stockDAO.update(stock)
        }
    }

    fun deleteStock(quote: StockTable) {
        viewModelScope.launch {
            stockDAO.delete(quote)
        }
    }

    private suspend fun checkExists(symbol: String?): Boolean {
        return stockDAO.checkExists(symbol) == 1
    }

    fun getAllFavourites(): LiveData<List<StockTable>> {
        return stockDAO.getAll()
    }

    fun updateAll() {
        viewModelScope.launch {
            for (symbol in stockDAO.getSymbols()) {
                val stock = service.getQuote(symbol, key)
                val update = PricesUpdate(
                    symbol,
                    stock.o,
                    stock.h,
                    stock.l,
                    stock.c,
                    stock.pc,
                    formatDecimalPlaces(priceChangeCalculation(stock.pc, stock.c)),
                    formatDecimalPlaces(priceChangePercentCalculation(stock.pc, stock.c)) + "%"
                )
                stockDAO.updatePrices(update)
            }
        }
    }
}