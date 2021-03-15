package com.android.stockfavourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.stockfavourites.data.RetrofitService
import com.android.stockfavourites.data.StockDAO
import com.android.stockfavourites.ui.main.FavouritesViewmodel
import kotlinx.coroutines.ExperimentalCoroutinesApi

class FavouritesViewmodelFactory(private val service: RetrofitService, private val stockDAO: StockDAO
) : ViewModelProvider.Factory {

    @ExperimentalCoroutinesApi
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavouritesViewmodel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavouritesViewmodel(service, stockDAO) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}