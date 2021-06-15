package com.android.stockfavourites.ui.main

import com.robinhood.spark.SparkAdapter

class SparkChartAdapter : SparkAdapter() {

    private var yData: FloatArray = floatArrayOf()

    fun setData(data: FloatArray) {
        yData = data
    }

    override fun getCount(): Int {
        return yData.size
    }

    override fun getItem(index: Int): Any {
        return yData[index]
    }

    override fun getY(index: Int): Float {
        return yData[index]
    }
}