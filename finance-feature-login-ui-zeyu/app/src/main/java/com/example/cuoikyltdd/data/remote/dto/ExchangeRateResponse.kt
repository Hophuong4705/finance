
package com.example.cuoikyltdd.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ExchangeRateResponse(
    @SerializedName("usdToVnd")
    val usdToVnd: Double = 0.0,

    @SerializedName("goldPrice")
    val goldPrice: Double = 0.0
)