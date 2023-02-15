package com.proxglobal.purchase.model

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetails.PricingPhase

class Phase(
    /**
     * Price that is formatted for each country
     */
    val price: String,

    /**
     * Code of currency
     */
    val currencyCode: String,

    /**
     * Period of phase
     */
    val billingPeriod: String,

    /**
     * Count of phase's period. For example, if [billingCycleCount] = 2, and [billingPeriod] = P1M,
     * that mean in this phase, user can buy offer 2 times, and period for each time is 1 month,
     * and total period user can have offer is 2 months
     */
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
                ProductDetails.RecurrenceMode.FINITE_RECURRING -> RecurrenceMode.FINITE_RECURRING
                ProductDetails.RecurrenceMode.INFINITE_RECURRING -> RecurrenceMode.INFINITE_RECURRING
                else -> RecurrenceMode.NON_RECURRING
            }
        ).apply {
            priceAmount = pricingPhase.priceAmountMicros
        }
    }

    private var priceAmount: Long = 0

    val isFreeTrial: Boolean
        get() = priceAmount == 0L

}

enum class RecurrenceMode {
    INFINITE_RECURRING,
    FINITE_RECURRING,
    NON_RECURRING
}