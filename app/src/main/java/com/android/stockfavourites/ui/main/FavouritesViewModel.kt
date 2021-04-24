package com.android.stockfavourites.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.stockfavourites.data.*
import com.android.stockfavourites.data.local.StockTable
import com.android.stockfavourites.models.SymbolLookup
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor (private val repository: StockRepository) :
    ViewModel() {

    //LiveData observed by fragment for notifications when update is complete and errors occur
    var refreshStatus = MutableLiveData(false)
    var errorType = MutableLiveData("")

    //Get stock details once autocomplete item selected
    fun getStock(symbol: String, companyName: String) {
        viewModelScope.launch {
            try {
                repository.getStock(symbol, companyName)
            } catch (e: Exception) {
                errorType.value = "Error searching stock"
                errorType.value = ""
                Log.i("ViewModel.searchStock", "Exception: $e")
            }
        }
    }

    //Search symbols for autocomplete
    suspend fun searchSymbol(symbol: String): SymbolLookup? =
        try {
            repository.searchSymbol(symbol)
        } catch (e: Exception){
            errorType.value = "Error searching symbols"
            errorType.value = ""
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
                repository.updateAllFavourites()
                refreshStatus.value = true
                refreshStatus.value = false
            }catch (e: Exception){
                errorType.value = "Error updating stocks"
                errorType.value = ""
                Log.i("ViewModel.updateAll", "Exception: $e")
            }
        }
    }
}