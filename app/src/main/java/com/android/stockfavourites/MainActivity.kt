package com.android.stockfavourites

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.stockfavourites.ui.main.FavouritesFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, FavouritesFragment.newInstance())
                    .commitNow()
        }
    }
}