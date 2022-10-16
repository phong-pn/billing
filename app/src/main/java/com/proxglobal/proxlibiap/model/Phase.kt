package com.proxglobal.proxlibiap.model

import com.android.billingclient.api.ProductDetails.PricingPhase

class Phase(
    val price: String,
    val currencyCode: String,
    val billingPeriod: String,
    val billingCycleCount: Int,
    val recurrenceMode: RecurrenceMode
) {
    companion object {
        @JvmStatic
        fun fromPricingPhase(pricingPhase: PricingPhase): Phase = Phase(
            pricingPhase.formattedPrice,
            pricingPhase.priceCurrencyCode,
            pricingPhase.billingPeriod,
            pricingPhase.billingCycleCount,
            when (pricingPhase.recurrenceMode) {
                com.android.billingclient.api.ProductDetails.RecurrenceMode.FINITE_RECURRING -> RecurrenceMode.FINITE_RECURRING
                com.android.billingclient.api.ProductDetails.RecurrenceMode.INFINITE_RECURRING -> RecurrenceMode.INFINITE_RECURRING
                else -> RecurrenceMode.NON_RECURRING
            }
        )
    }
}

enum class RecurrenceMode {
    INFINITE_RECURRING,
    FINITE_RECURRING,
    NON_RECURRING
}