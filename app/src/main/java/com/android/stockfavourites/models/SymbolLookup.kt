package com.android.stockfavourites.models
import com.squareup.moshi.JsonClass

import com.squareup.moshi.Json


@JsonClass(generateAdapter = true)
data class SymbolLookup(
    @Json(name = "count")
    val count: Int?,
    @Json(name = "result")
    val result: List<Result?>?
) {
    @JsonClass(generateAdapter = true)
    data class Result(
        @Json(name = "description")
        val description: String?,
        @Json(name = "displaySymbol")
        val displaySymbol: String?,
        @Json(name = "symbol")
        val symbol: String?,
        @Json(name = "type")
        val type: String?,
        @Json(name = "primary")
        val primary: List<String?>?
    )
}