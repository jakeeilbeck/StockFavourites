package com.android.stockfavourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.stockfavourites.data.RetrofitService
import com.android.stockfavourites.data.StockDAO
import com.android.stockfavourites.ui.main.FavouritesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

class FavouritesViewModelFactory(private val service: RetrofitService, private val stockDAO: StockDAO
) : ViewModelProvider.Factory {

    @ExperimentalCoroutinesApi
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavouritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavouritesViewModel(service, stockDAO) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}