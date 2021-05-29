package com.android.stockfavourites.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.stockfavourites.data.*
import com.android.stockfavourites.data.local.StockTable
import com.android.stockfavourites.models.SymbolLookup
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor (private val repository: StockRepository) :
    ViewModel() {

    val errorFlow = MutableSharedFlow<String>(0)
    val loadingFlow = MutableStateFlow(false)

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
                setLoadingStatus(false)
                errorFlow.emit("Stocks updated")
            }catch (e: Exception){
                setLoadingStatus(false)
                errorFlow.emit("Error updating stocks")
                Log.i("ViewModel.updateAll", "Exception: $e")
            }
        }
    }
}