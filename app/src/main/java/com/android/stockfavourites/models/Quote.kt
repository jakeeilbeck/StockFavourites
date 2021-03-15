package com.android.stockfavourites.models
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json

@JsonClass(generateAdapter = true)
data class Quote(
    @Json(name = "c")
    val c: Double?,
    @Json(name = "h")
    val h: Double?,
    @Json(name = "l")
    val l: Double?,
    @Json(name = "o")
    val o: Double?,
    @Json(name = "pc")
    val pc: Double?,
    @Json(name = "t")
    val t: Int?
)