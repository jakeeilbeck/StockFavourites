package com.android.stockfavourites.data.local

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType

class CandleTypeConverters {

    private val moshi = Moshi.Builder().build()
    private val listMyData : ParameterizedType = Types.newParameterizedType(List::class.javaObjectType, Double::class.javaObjectType)
    private val jsonAdapter: JsonAdapter<List<Double?>?> = moshi.adapter(listMyData)

    @TypeConverter
    fun listMyModelToJsonStr(listMyModel: List<Double?>?): String? {
        return jsonAdapter.toJson(listMyModel)
    }

    @TypeConverter
    fun jsonStrToListMyModel(jsonStr: String?): List<Double?>? {
        return jsonStr?.let { jsonAdapter.fromJson(jsonStr) }
    }
}