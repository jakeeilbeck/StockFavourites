package com.android.stockfavourites.data

import androidx.lifecycle.LiveData
import com.android.stockfavourites.data.local.PricesUpdate
import com.android.stockfavourites.data.local.StockDAO
import com.android.stockfavourites.data.local.StockTable
import com.android.stockfavourites.data.remote.StockApi
import com.android.stockfavourites.models.Quote
import com.android.stockfavourites.models.SymbolLookup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepository @Inject constructor(
    private val stockApi: StockApi,
    private val stockDAO: StockDAO
) {

    private val key = "c17rnj748v6sj55b58n0"

    //Get stock details once autocomplete item selected
    suspend fun getStock(symbol: String, companyName: String) {
        val stock = stockApi.getQuote(symbol, key)
        addToFavourites(symbol, companyName, stock)
    }

    //Search symbols for autocomplete
    suspend fun searchSymbol(symbol: String): SymbolLookup {
        return stockApi.getSymbols(symbol, key)
    }

    private suspend fun addToFavourites(symbol: String, companyName: String, stock: Quote) {
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

    private fun formatDecimalPlaces(number: Double?): String {
        return "%.2f".format(number)
    }

    private fun priceChangeCalculation(close: Double?, current: Double?): Double? {
        return close?.let { current?.minus(it) }
    }

    private fun priceChangePercentCalculation(close: Double?, current: Double?): Double? {
        return ((close?.let { current?.minus(it) })?.div(close))?.times(100)
    }

    private suspend fun insertFavourite(stock: StockTable) {
        if (!checkIfCurrentFavourite(stock.symbol)) {
            stockDAO.insert(stock)
        } else {
            updateExisting(stock)
        }
    }

    private suspend fun updateExisting(stock: StockTable) {
        stockDAO.update(stock)
    }

    suspend fun deleteFavourite(quote: StockTable) {
        stockDAO.delete(quote)
    }

    private suspend fun checkIfCurrentFavourite(symbol: String?): Boolean {
        return stockDAO.checkExists(symbol) == 1
    }

    fun getAllFavourites(): LiveData<List<StockTable>> {
        return stockDAO.getAll()
    }

    suspend fun updateAllFavourites() {
        for (symbol in stockDAO.getSymbols()) {
            val stock = stockApi.getQuote(symbol, key)
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