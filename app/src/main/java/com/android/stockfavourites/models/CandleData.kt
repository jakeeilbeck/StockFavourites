package com.android.stockfavourites.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CandleData(
    @Json(name = "c")
    val c: List<Double?>?,
    @Json(name = "h")
    val h: List<Double?>?,
    @Json(name = "l")
    val l: List<Double?>?,
    @Json(name = "o")
    val o: List<Double?>?,
    @Json(name = "s")
    val s: String?,
    @Json(name = "t")
    val t: MutableList<Int?>?,
    @Json(name = "v")
    val v: List<Int?>?
)