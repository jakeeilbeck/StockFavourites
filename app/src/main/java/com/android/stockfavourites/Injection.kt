package com.android.stockfavourites

import androidx.lifecycle.ViewModelProvider
import com.android.stockfavourites.data.RetrofitService
import com.android.stockfavourites.data.StockDAO

object Injection {
    fun provideFavouritesViewmodelFactory(stockDAO: StockDAO): ViewModelProvider.Factory{
        return FavouritesViewmodelFactory(RetrofitService.create(), stockDAO)
    }
}